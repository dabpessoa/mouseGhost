package me.dabpessoa.mouseghost.utils.textarea;

import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import javafx.application.Platform;
import javafx.scene.control.TextArea;
import lombok.Setter;

import java.nio.charset.StandardCharsets;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

@Setter
public class TextAreaAppender extends AppenderBase<ILoggingEvent> {

    private static final int MAX_CHARS = 200_000;

    private static volatile TextArea textArea;
    private static final Queue<String> PENDING = new ConcurrentLinkedQueue<>();
    private static final AtomicBoolean FLUSH_SCHEDULED = new AtomicBoolean(false);

    // Chamado pelo Logback a partir da tag <encoder> do XML
    private PatternLayoutEncoder encoder;

    /** Chame uma vez, quando a TextArea já existir */
    public static void setTextArea(TextArea area) {
        textArea = area;
        scheduleFlush();
    }

    @Override
    public void start() {
        if (encoder == null) {
            addError("Encoder não configurado para o appender " + getName());
            return;
        }
        super.start();
    }

    @Override
    protected void append(ILoggingEvent event) {
        PENDING.add(new String(encoder.encode(event), StandardCharsets.UTF_8));
        scheduleFlush();
    }

    private static void scheduleFlush() {
        if (textArea != null && FLUSH_SCHEDULED.compareAndSet(false, true)) {
            Platform.runLater(TextAreaAppender::flush);
        }
    }

    private static void flush() {
        FLUSH_SCHEDULED.set(false);
        TextArea area = textArea;
        if (area == null) {
            return;
        }

        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = PENDING.poll()) != null) {
            sb.append(line);
        }

        if (sb.isEmpty()) {
            return;
        }

        area.appendText(sb.toString());

        // Evita crescer sem limite e travar a interface
        int excesso = area.getLength() - MAX_CHARS;
        if (excesso > 0) {
            area.deleteText(0, excesso);
        }
    }

}