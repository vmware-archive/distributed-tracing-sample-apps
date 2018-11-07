package com.wfsample.constants;

import com.wavefront.sdk.common.WavefrontSender;
import com.wavefront.sdk.direct.ingestion.WavefrontDirectIngestionClient;

public abstract class WavefrontConnection {
    private static final String WFHOST = "https://tracing.wavefront.com"; // Should be clearer in docs
    private static final String TOKEN = "104c7c31-598d-46e2-9972-0fd6c1ec8285";

    public static WavefrontSender getSender() {
        WavefrontDirectIngestionClient.Builder builder =
                new WavefrontDirectIngestionClient.Builder(WFHOST, TOKEN);
        return builder.build();
    }
}
