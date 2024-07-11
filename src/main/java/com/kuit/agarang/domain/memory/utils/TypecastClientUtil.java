package com.kuit.agarang.domain.memory.utils;

import com.kuit.agarang.domain.memory.dto.TypecastRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TypecastClientUtil {

  private final WebClientUtil webClientUtil;
  @Value("${typecast.baseUrl}")
  private String baseUrl;
  @Value("${typecast.apiKey}")
  private String apiKey;

  public <T> T post(String uri, TypecastRequest requestDto, Class<T> responseClass) {
    return webClientUtil.post(baseUrl + uri, apiKey, requestDto, responseClass);
  }

  public <T> T get(String url, Class<T> responseClass) {
    return webClientUtil.get(url, apiKey, responseClass);
  }
}
