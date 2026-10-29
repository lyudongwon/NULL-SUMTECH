package com.nullteam.foodcheck.domain.judgment.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nullteam.foodcheck.domain.judgment.dto.JudgmentRequest;
import com.nullteam.foodcheck.domain.judgment.dto.JudgmentResponse;
import com.nullteam.foodcheck.domain.judgment.dto.JudgmentResult;
import com.nullteam.foodcheck.domain.judgment.dto.ManualJudgmentRequest;
import com.nullteam.foodcheck.domain.judgment.service.JudgmentService;

/** REQ-3 3단계 판정·REQ-6 근거 상세와 OPT-1 수동 입력 판정 (docs/api.md). */
@RestController
@RequestMapping("/api/v1/judgments")
public class JudgmentController {

	private final JudgmentService judgmentService;

	public JudgmentController(JudgmentService judgmentService) {
		this.judgmentService = judgmentService;
	}

	@PostMapping
	public JudgmentResponse judge(@RequestBody(required = false) JudgmentRequest request) {
		return judgmentService.judge(request);
	}

	@PostMapping("/manual")
	public JudgmentResult judgeManual(@RequestBody(required = false) ManualJudgmentRequest request) {
		return judgmentService.judgeManual(request);
	}
}
