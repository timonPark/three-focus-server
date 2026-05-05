<!-- part 1/2 of testing-anti-patterns.md -->

# Testing Anti-Patterns

---

## Core Principle

> **"Test what the code does, not what the mocks do."**

When tests verify mock behavior instead of actual functionality, they provide false confidence while catching zero real bugs.

---

## The Five Anti-Patterns

### Anti-Pattern 1: Testing Mock Behavior

**The Problem:** Verifying that mocks exist and were called, rather than testing actual component output.

```typescript
// ❌ BAD: Testing the mock, not the behavior
it('should call the API', () => {
  const mockApi = jest.fn().mockResolvedValue({ data: 'test' });
  const service = new UserService(mockApi);

  service.getUser(1);

  expect(mockApi).toHaveBeenCalledWith(1); // Testing mock, not result
});
```

```typescript
// ✅ GOOD: Testing actual behavior
it('should return user data from API', async () => {
  const mockApi = jest.fn().mockResolvedValue({ id: 1, name: 'Alice' });
  const service = new UserService(mockApi);

  const user = await service.getUser(1);

  expect(user.name).toBe('Alice'); // Testing actual output
});
```

**Solution:** Test the genuine component output. If you can only verify mock calls, reconsider whether the test adds value.

---

### Anti-Pattern 2: Test-Only Methods in Production

**The Problem:** Adding methods to production classes solely for test setup or cleanup.

```typescript
// ❌ BAD: Production code polluted with test concerns
class UserCache {
  private cache: Map<number, User> = new Map();

  getUser(id: number): User | undefined {
    return this.cache.get(id);
  }

  // This method exists ONLY for tests
  _resetForTesting(): void {
    this.cache.clear();
  }
}
```

```typescript
// ✅ GOOD: Test utilities separate from production
// production/UserCache.ts
class UserCache {
  private cache: Map<number, User> = new Map();

  getUser(id: number): User | undefined {
    return this.cache.get(id);
  }
}

// test/helpers.ts
function createFreshCache(): UserCache {
  return new UserCache(); // Fresh instance per test
}
```

**Solution:** Relocate cleanup logic to test utility functions. Use fresh instances per test instead of reset methods.

---

### Anti-Pattern 3: Mocking Without Understanding

**The Problem:** Over-mocking without grasping side effects, leading to tests that pass but hide real issues.

```typescript
// ❌ BAD: Mocking everything without understanding
it('should process order', async () => {
  jest.mock('./inventory');
  jest.mock('./payment');
  jest.mock('./shipping');
  jest.mock('./notifications');

  const result = await processOrder(order);

  expect(result.success).toBe(true); // What did we actually test?
});
```

```typescript
// ✅ GOOD: Strategic mocking with real components where possible
it('should process order with real inventory check', async () => {
  // Real inventory service against test database
  const inventory = new InventoryService(testDb);

  // Mock only external services
  const payment = mockPaymentGateway();

  const processor = new OrderProcessor(inventory, payment);
  const result = await processor.process(order);

  expect(result.success).toBe(true);
  expect(await inventory.getStock(order.itemId)).toBe(originalStock - 1);
});
```

**Solution:** Run tests with real implementations first to understand behavior. Then mock at the appropriate level - external services, not internal logic.

---
