package team.projectpulse.ram;

import org.junit.jupiter.api.Test;
import team.projectpulse.ram.document.DocumentSectionLock;
import team.projectpulse.ram.usecase.UseCaseLock;

import java.time.Instant;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LockStateTest {

    @Test
    void documentSectionLockPreservesExpiryAndNullHandling() {
        DocumentSectionLock lock = new DocumentSectionLock();
        verifyLockStates((lockedAt, expiresAt) -> lock.lock(null, lockedAt, expiresAt, null), lock::isLocked);
    }

    @Test
    void useCaseLockPreservesExpiryAndNullHandling() {
        UseCaseLock lock = new UseCaseLock();
        verifyLockStates((lockedAt, expiresAt) -> lock.lock(null, lockedAt, expiresAt, null), lock::isLocked);
    }

    private void verifyLockStates(BiConsumer<Instant, Instant> initialize, Predicate<Instant> isLocked) {
        Instant now = Instant.parse("2026-11-11T12:00:00Z");

        initialize.accept(null, null);
        assertThat(isLocked.test(now)).isFalse();
        assertThat(isLocked.test(null)).isFalse();

        // Without a lock timestamp, even a future expiry does not make the object locked.
        initialize.accept(null, now.plusSeconds(60));
        assertThat(isLocked.test(now)).isFalse();

        // A manual lock has no expiry and does not need to inspect the supplied current time.
        initialize.accept(now.minusSeconds(60), null);
        assertThat(isLocked.test(now)).isTrue();
        assertThat(isLocked.test(null)).isTrue();

        initialize.accept(now.minusSeconds(60), now.plusSeconds(60));
        assertThat(isLocked.test(now)).isTrue();
        assertThatThrownBy(() -> isLocked.test(null)).isInstanceOf(NullPointerException.class);

        initialize.accept(now.minusSeconds(60), now);
        assertThat(isLocked.test(now)).isFalse();

        initialize.accept(now.minusSeconds(60), now.minusSeconds(1));
        assertThat(isLocked.test(now)).isFalse();
    }
}
