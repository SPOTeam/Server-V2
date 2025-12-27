package kr.spot.view;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ViewableType {

    POST("post"),
    STUDY("study"),
    STUDY_BOARD("study-board");

    private final String keyPrefix;
}
