package com.example.demo.dto;

import java.time.LocalDateTime;

import com.example.demo.calendar.entity.PostStatus;
import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ScheduleRequestDto {
	private Long postId;
	private String scheduledDate;
	private PostStatus status;
}