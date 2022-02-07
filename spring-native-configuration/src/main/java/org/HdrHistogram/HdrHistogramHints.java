package org.HdrHistogram;

import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.TypeHint;
import org.springframework.nativex.type.NativeConfiguration;

@NativeHint(trigger = org.HdrHistogram.Histogram.class, types =
@TypeHint(
		types = {
				org.HdrHistogram.Histogram.class,
				org.HdrHistogram.ConcurrentHistogram.class
		})
)
public class HdrHistogramHints implements NativeConfiguration {
}
