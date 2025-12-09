package kr.spot.infrastructure.jpa.querydsl.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class StudyApplicationInfo {

  private Long studyMemberId;
  private Long studyId;
  private String studyName;
  private String studyProfileImageUrl;
}