package fi.jakojaannos.roguelite.engine.view.sprite;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
public class Animation {
    private final List<Frame> frames;
    private final double totalDuration;

    private Animation(final List<Frame> frames) {
        this.frames = frames;
        this.totalDuration = this.frames.stream()
                                        .mapToDouble(frame -> frame.duration)
                                        .sum();
    }

    public int getFrameIndexOfFrame(int n) {
        return this.frames.get(n).index;
    }

    public int getFrameIndexAtTime(final double time) {
        var t = time % this.totalDuration;
        int i = -1;
        do {
            ++i;
            t -= this.frames.get(i).duration;
        } while (t > 0);

        return this.frames.get(i).index;
    }

    public static Animation forSingleFrame(final int index, final double duration) {
        return new Animation(List.of(new Frame(index, duration)));
    }

    public static Animation forFrameRange(
            final int firstFrame,
            final int lastFrame,
            final double totalDuration
    ) {
        val count = lastFrame - firstFrame + 1;
        val durationPerFrame = totalDuration / count;
        return new Animation(IntStream.rangeClosed(firstFrame, lastFrame)
                                      .mapToObj(index -> new Frame(index, durationPerFrame))
                                      .collect(Collectors.toUnmodifiableList()));
    }

    public static Animation forFrames(
            final List<Frame> frames
    ) {
        return new Animation(List.copyOf(frames));
    }

    @RequiredArgsConstructor
    public static class Frame {
        @Getter private final int index;
        @Getter private final double duration;
    }
}
