package com.example.demo.dto;

import com.example.demo.domain.calendar.entity.ContentStatus;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ScheduleRequestDto {
	private Long contentId;;
	private String scheduledDate;
	private ContentStatus status;
}