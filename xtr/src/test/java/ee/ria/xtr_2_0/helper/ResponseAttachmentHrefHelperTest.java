package ee.ria.xtr_2_0.helper;

import ee.ria.xtr_2_0.exception.HrefMissingException;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ResponseAttachmentHrefHelperTest {

    @Test
    void missingHrefThrowsException() {
        assertThatThrownBy(() -> ResponseAttachmentHrefHelper.href("bar", new Foo(new Bar(null)))).isInstanceOf(HrefMissingException.class);
    }

    @Test
    void testGetHref() {
        String href = "asd-qwe-fdg";
        assertThat(ResponseAttachmentHrefHelper.href("bar#baz", new Foo(new Bar(new Baz(href))))).isEqualTo(href);
    }

    @Data
    @RequiredArgsConstructor
    public class Foo {

        private final Bar bar;

    }

    @Data
    @RequiredArgsConstructor
    public class Bar {

        private final Baz baz;

    }

    @Data
    @RequiredArgsConstructor
    public class Baz {

        private final String href;

    }

}
