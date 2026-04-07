package com.example.demo.dto;

import com.example.demo.calendar.entity.PostStatus;
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