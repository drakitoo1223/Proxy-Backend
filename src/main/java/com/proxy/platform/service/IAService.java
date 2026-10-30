package com.proxy.platform.service;

import com.proxy.platform.model.GenerationRequest;
import com.proxy.platform.model.GenerationResponse;

public interface IAService {
    GenerationResponse generate(GenerationRequest request);
}
