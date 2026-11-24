const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

function validProfilePayload(uid, created, updated) {
  return {
    userId: uid,
    displayName: "Yasin Tunga",
    email: "yasintunga@gmail.com",
    department: "Acil Servis",
    professionGroup: "healthcare",
    title: "Uzman",
    cyclePreset: "CYCLE_12_36",
    referenceStartDate: "2026-11-01",
    customSequence: ["GUNDUZ", "GECE", "IZIN"],
    appLanguage: "tr",
    schemaVersion: 1,
    createdAt: created,
    updatedAt: updated,
  };
}

function validOverridePayload(uid, dateIso, created, updated) {
  return {
    userId: uid,
    dateIso,
    shiftType: "GECE",
    overtimeHours: 2.5,
    note: "Gece nöbeti değişimi",
    customStartTime: "20:00",
    customEndTime: "08:00",
    createdAt: created,
    updatedAt: updated,
  };
}

function validColleaguePayload(uid, created, updated) {
  return {
    userId: uid,
    name: "Ahmet Yılmaz",
    department: "Güvenlik",
    cyclePreset: "CYCLE_12_36",
    referenceStartDate: "2026-11-01",
    customSequence: ["GUNDUZ", "GECE", "IZIN"],
    sourceLabel: "QR",
    createdAt: created,
    updatedAt: updated,
  };
}

test("Unauthenticated users cannot read or write any collection", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  const now = new Date(Date.now() - 2000);

  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(unauthDb.collection("users").get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).set(validProfilePayload(ALICE_UID, now, now)));
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("shift_overrides").get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("colleagues").get());
});

test("Authenticated owner: valid Kotlin UserProfile create, partial update, and delete succeed; shadow & immutable violations fail", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const created = new Date(Date.now() - 10000);
  const updated = new Date(Date.now() - 2000);

  // Create profile (exact Kotlin createPayload — NO @DocumentId 'id' key)
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set(validProfilePayload(ALICE_UID, created, created))
  );

  // Read own profile succeeds; listing /users is always denied
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).get());
  await assertFails(aliceDb.collection("users").get());

  // Partial update preserves immutable createdAt & userId
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).update({
      displayName: "Yasin T.",
      appLanguage: "en",
      updatedAt: updated,
    })
  );

  // Reject mutating immutable createdAt
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      createdAt: updated,
      updatedAt: updated,
    })
  );

  // Reject ghost field (Shadow Update Test)
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      isAdmin: true,
      updatedAt: updated,
    })
  );

  // Reject future timestamp
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      displayName: "Future",
      updatedAt: new Date(Date.now() + 60000),
    })
  );

  // Delete own profile succeeds
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).delete());

  // Reject create containing forbidden @DocumentId 'id' key
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).set({
      id: ALICE_UID,
      ...validProfilePayload(ALICE_UID, created, created),
    })
  );
});

test("Authenticated owner: shift_overrides create, partial update, list, delete succeed and invalid payloads fail", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const created = new Date(Date.now() - 10000);
  const updated = new Date(Date.now() - 1000);
  const dateKey = "2026-11-19";

  // Owner list on empty subcollection must succeed
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).collection("shift_overrides").get());

  // Create valid override
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("shift_overrides").doc(dateKey)
      .set(validOverridePayload(ALICE_UID, dateKey, created, created))
  );

  // Partial update
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("shift_overrides").doc(dateKey)
      .update({
        overtimeHours: 4.0,
        note: "Ek mesai güncellendi",
        updatedAt: updated,
      })
  );

  // Reject invalid shiftType enum
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("shift_overrides").doc(dateKey)
      .update({
        shiftType: "INVALID_SHIFT",
        updatedAt: updated,
      })
  );

  // Reject out-of-bounds overtime (> 24)
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("shift_overrides").doc(dateKey)
      .update({
        overtimeHours: 30,
        updatedAt: updated,
      })
  );

  // Reject dateIso mismatch with document key
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("shift_overrides").doc("2026-11-20")
      .set(validOverridePayload(ALICE_UID, "2026-11-21", created, created))
  );

  // Delete succeeds
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("shift_overrides").doc(dateKey).delete()
  );
});

test("Authenticated owner: colleagues create, update, list, delete succeed and bounds enforced", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const created = new Date(Date.now() - 10000);
  const updated = new Date(Date.now() - 1000);

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("colleagues").doc("col_1")
      .set(validColleaguePayload(ALICE_UID, created, created))
  );

  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).collection("colleagues").get());

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("colleagues").doc("col_1")
      .update({
        department: "Acil",
        updatedAt: updated,
      })
  );

  // Reject oversized customSequence (> 31)
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("colleagues").doc("col_1")
      .update({
        customSequence: Array(35).fill("GUNDUZ"),
        updatedAt: updated,
      })
  );

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("colleagues").doc("col_1").delete()
  );
});

test("Cross-user isolation: Alice cannot read, list, write, or delete Bob's data", async () => {
  const created = new Date(Date.now() - 10000);
  await testEnv.withSecurityRulesDisabled(async (context) => {
    const adminDb = context.firestore();
    await adminDb.collection("users").doc(BOB_UID).set(validProfilePayload(BOB_UID, created, created));
    await adminDb.collection("users").doc(BOB_UID).collection("shift_overrides").doc("2026-11-19")
      .set(validOverridePayload(BOB_UID, "2026-11-19", created, created));
    await adminDb.collection("users").doc(BOB_UID).collection("colleagues").doc("col_bob")
      .set(validColleaguePayload(BOB_UID, created, created));
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(aliceDb.collection("users").doc(BOB_UID).get());
  await assertFails(aliceDb.collection("users").doc(BOB_UID).update({ displayName: "Hijacked", updatedAt: new Date(Date.now() - 1000) }));
  await assertFails(aliceDb.collection("users").doc(BOB_UID).delete());

  await assertFails(aliceDb.collection("users").doc(BOB_UID).collection("shift_overrides").get());
  await assertFails(aliceDb.collection("users").doc(BOB_UID).collection("shift_overrides").doc("2026-11-19").get());
  await assertFails(aliceDb.collection("users").doc(BOB_UID).collection("shift_overrides").doc("2026-11-19").delete());

  await assertFails(aliceDb.collection("users").doc(BOB_UID).collection("colleagues").get());
  await assertFails(aliceDb.collection("users").doc(BOB_UID).collection("colleagues").doc("col_bob").get());
  await assertFails(aliceDb.collection("users").doc(BOB_UID).collection("colleagues").doc("col_bob").delete());
});
