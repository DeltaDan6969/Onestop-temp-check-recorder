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

test("Unauthenticated user: cannot read daily logs", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("daily_logs").get());
});

test("Unauthenticated user: cannot write daily logs", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).collection("daily_logs").doc("2026-10-25").set({
      userId: ALICE_UID,
      date: "2026-10-25",
      dayOfWeek: "Sunday",
      checkedBy: "Staff A",
      itemsCount: 0,
      items: [],
      createdAt: new Date(),
      updatedAt: new Date(),
    })
  );
});

test("Authenticated user: cannot read another user's daily logs", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("users").doc(BOB_UID).collection("daily_logs").doc("2026-10-25").set({
      userId: BOB_UID,
      date: "2026-10-25",
      dayOfWeek: "Sunday",
      checkedBy: "Bob",
      itemsCount: 0,
      items: [],
      createdAt: new Date(),
      updatedAt: new Date(),
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(aliceDb.collection("users").doc(BOB_UID).collection("daily_logs").doc("2026-10-25").get());
});

test("Authenticated user: can create and read their own daily log", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("daily_logs").doc("2026-10-25").set({
      userId: ALICE_UID,
      date: "2026-10-25",
      dayOfWeek: "Sunday",
      checkedBy: "Alice",
      itemsCount: 1,
      items: [
        {
          productId: "p1",
          productName: "bacon cheese turnover",
          coreTemp: 204.5,
          t1Temp: 88.2,
        },
      ],
      createdAt: new Date(),
      updatedAt: new Date(),
    })
  );

  const doc = await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("daily_logs").doc("2026-10-25").get()
  );
  assert(doc.exists);
});

function assert(condition) {
  if (!condition) throw new Error("Assertion failed");
}
