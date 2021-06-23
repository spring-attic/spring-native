package org.springframework.synthesizer;

import org.junit.Before;
import org.junit.Test;
import org.springframework.SynthesizerComputationComponentProcessor;
import org.springframework.nativex.type.TypeSystem;
import org.springframework.nativex.util.NativeTestContext;
import org.springframework.synthesizer.components.Controller;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;

public class SynthesizerComputationComponentProcessorTest {

    private NativeTestContext nativeContext = new NativeTestContext();
    private TypeSystem typeSystem = nativeContext.getTypeSystem();
    private SynthesizerComputationComponentProcessor processor;

    @Before
    public void setUp() {
        processor = new SynthesizerComputationComponentProcessor();
    }

    @Test
    public void shouldDetectAnnotationOnInterfaces() {
        String componentType = typeSystem.resolve(Controller.class).getDottedName();

        // handled
        assertThat(processor.handle(nativeContext, componentType, Collections.emptyList())).isTrue();

        // processed
        processor.process(nativeContext, componentType, Collections.emptyList());

        // and has proxies for annotations in interface
        assertThat(nativeContext.getProxyEntries().keySet()).isEqualTo(new HashSet<>(Arrays.asList(
                "org.springframework.stereotype.Component",
                "org.springframework.web.bind.annotation.RequestMapping",
                "org.springframework.web.bind.annotation.RequestParam")));
    }
}
