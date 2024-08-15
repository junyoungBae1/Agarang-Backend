package com.kuit.agarang.domain.home.service;

import com.kuit.agarang.domain.ai.service.AIService;
import com.kuit.agarang.domain.baby.model.entity.Baby;
import com.kuit.agarang.domain.baby.model.entity.Character;
import com.kuit.agarang.domain.baby.repository.BabyRepository;
import com.kuit.agarang.domain.home.model.dto.HomeResponse;
import com.kuit.agarang.domain.member.model.entity.Member;
import com.kuit.agarang.domain.member.repository.MemberRepository;
import com.kuit.agarang.domain.memory.model.entity.Memory;
import com.kuit.agarang.domain.memory.repository.MemoryRepository;
import com.kuit.agarang.global.common.exception.exception.BusinessException;
import com.kuit.agarang.global.common.model.dto.BaseResponseStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HomeService {

  private final BabyRepository babyRepository;
  private final MemberRepository memberRepository;
  private final MemoryRepository memoryRepository;
  private final AIService aiService;

  public HomeResponse getHome(String providerId) {

    LocalDate today = LocalDate.now();
    log.info("today = {}", today);

    Baby baby = babyRepository.findByProviderId(providerId)
        .orElseThrow(() -> new BusinessException(BaseResponseStatus.NOT_FOUND_BABY));
    String babyName = baby.getName();

    LocalDate dueDate = baby.getDueDate();
    Integer dDay = (int) ChronoUnit.DAYS.between(today, dueDate);
    log.info("dDay = {}", dDay);

    Character character = baby.getCharacter();
    String characterUrl = character.getImageUrl();

    // 말풍선
    Member member = memberRepository.findByProviderId(providerId)
        .orElseThrow(() -> new BusinessException(BaseResponseStatus.NOT_FOUND_MEMBER));
    String speechBubble = aiService.getCharacterBubble(character, member.getFamilyRole());

    // 최근 태교 카드 Id
    List<Memory> recentImages = memoryRepository.findTop3ByBabyOrderByCreatedAtDesc(baby);
    List<Long> recentImageIds = recentImages.stream()
        .map(Memory::getId).collect(Collectors.toList());

    return HomeResponse.builder()
        .today(today)
        .babyName(babyName)
        .dDay(dDay)
        .characterUrl(characterUrl)
        .speechBubble(speechBubble)
        .memoryIds(recentImageIds).build();
  }
}
