package me.dabpessoa.mouseghost.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SymbolLookup;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.util.OptionalLong;

/**
 * Consulta a última entrada do usuário no Windows, incluindo teclado e mouse.
 * A API só é usada para saber se houve teclado desde o último ciclo; nenhum
 * conteúdo digitado é capturado ou armazenado.
 */
@Slf4j
@Component
public class KeyboardActivityMonitor {

    private static final int LAST_INPUT_INFO_SIZE = 8;
    private static final int LAST_INPUT_INFO_ALIGNMENT = 4;
    private static final int LAST_INPUT_TIME_OFFSET = 4;

    private final NativeFunctions nativeFunctions;

    public KeyboardActivityMonitor() {
        nativeFunctions = createNativeFunctions();
    }

    public OptionalLong getLastInputAtMillis() {
        if (nativeFunctions == null) {
            return OptionalLong.empty();
        }

        try (Arena arena = Arena.ofConfined()) {
            MemorySegment lastInputInfo = arena.allocate(
                    LAST_INPUT_INFO_SIZE, LAST_INPUT_INFO_ALIGNMENT);
            lastInputInfo.set(ValueLayout.JAVA_INT, 0, LAST_INPUT_INFO_SIZE);

            int success = (int) nativeFunctions.getLastInputInfo().invoke(lastInputInfo);
            if (success == 0) {
                return OptionalLong.empty();
            }

            long currentTick = Integer.toUnsignedLong(
                    (int) nativeFunctions.getTickCount().invoke());
            long lastInputTick = Integer.toUnsignedLong(
                    lastInputInfo.get(ValueLayout.JAVA_INT, LAST_INPUT_TIME_OFFSET));
            long idleMillis = unsignedTickDifference(currentTick, lastInputTick);
            return OptionalLong.of(System.currentTimeMillis() - idleMillis);
        } catch (Throwable exception) {
            log.debug("Não foi possível consultar a última entrada do teclado.", exception);
            return OptionalLong.empty();
        }
    }

    private NativeFunctions createNativeFunctions() {
        if (!System.getProperty("os.name", "").toLowerCase().contains("win")) {
            return null;
        }

        try {
            Linker linker = Linker.nativeLinker();
            SymbolLookup user32 = SymbolLookup.libraryLookup("user32.dll", Arena.global());
            SymbolLookup kernel32 = SymbolLookup.libraryLookup("kernel32.dll", Arena.global());

            MethodHandle getLastInputInfo = linker.downcallHandle(
                    user32.findOrThrow("GetLastInputInfo"),
                    FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS));
            MethodHandle getTickCount = linker.downcallHandle(
                    kernel32.findOrThrow("GetTickCount"),
                    FunctionDescriptor.of(ValueLayout.JAVA_INT));
            return new NativeFunctions(getLastInputInfo, getTickCount);
        } catch (RuntimeException exception) {
            log.warn("Detecção global de teclado indisponível; usando apenas o mouse.");
            return null;
        }
    }

    private long unsignedTickDifference(long currentTick, long previousTick) {
        return currentTick >= previousTick
                ? currentTick - previousTick
                : (1L << Integer.SIZE) - previousTick + currentTick;
    }

    private record NativeFunctions(MethodHandle getLastInputInfo, MethodHandle getTickCount) {
    }
}
