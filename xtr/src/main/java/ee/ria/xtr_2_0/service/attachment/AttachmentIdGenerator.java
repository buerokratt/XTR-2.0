package ee.ria.xtr_2_0.service.attachment;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AttachmentIdGenerator {

    private static long salt;

    /**
     *
     * @return id String
     */
    public static String getId() {
        return RandomStringUtils.randomAlphanumeric(10) + ++salt;
    }

}
