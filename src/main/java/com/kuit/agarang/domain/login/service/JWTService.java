package com.kuit.agarang.domain.login.service;

import com.kuit.agarang.domain.login.model.dto.ReissueDto;
import com.kuit.agarang.domain.login.utils.JWTUtil;
import com.kuit.agarang.domain.member.model.entity.Member;
import com.kuit.agarang.domain.member.repository.MemberRepository;
import com.kuit.agarang.global.common.exception.exception.BusinessException;
import com.kuit.agarang.global.common.service.RedisService;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.kuit.agarang.global.common.model.dto.BaseResponseStatus.*;

@Service
@Transactional
@RequiredArgsConstructor
public class JWTService {

  private final JWTUtil jwtUtil;
  private final RedisService redisService;
  private final MemberRepository memberRepository;

  public ReissueDto reissueTokens(String oldRefresh) {

    // RefreshToken 유효성 검증
    Member member = validateRefreshToken(oldRefresh);

    // AccessToken 생성 및 Refresh Rotate
    String newAccess = jwtUtil.createAccessToken(member.getProviderId(), member.getRole(), member.getId());
    String newRefresh = jwtUtil.createRefreshToken(member.getProviderId(), member.getRole(), member.getId());

    // Refresh Token Update
    redisService.delete(oldRefresh);
    redisService.save(newRefresh, member.getId());

    return ReissueDto.builder()
      .newAccessToken(newAccess)
      .newRefreshToken(newRefresh)
      .providerId(member.getProviderId())
      .role(member.getRole())
      .build();
  }

  private Member validateRefreshToken(String oldRefresh) {
    if (oldRefresh == null) {
      throw new BusinessException(NOT_FOUND_REFRESH_TOKEN);
    }

    Long memberId = redisService.get(oldRefresh, Long.class)
      .orElseThrow(() -> new BusinessException(NOT_FOUND_REFRESH_TOKEN));

    try {
      jwtUtil.isExpired(oldRefresh);
    } catch (ExpiredJwtException e) {
      throw new BusinessException(EXPIRED_REFRESH_TOKEN);
    }

    String category = jwtUtil.getCategory(oldRefresh);
    if (!category.equals("refresh")) {
      throw new BusinessException(INVALID_REFRESH_TOKEN);
    }

    return memberRepository.findById(memberId)
      .orElseThrow(() -> new BusinessException(NOT_FOUND_MEMBER));
  }
}
