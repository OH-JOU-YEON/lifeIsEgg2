package com.ohjeon.life_is_egg.domain.report.service;

import com.ohjeon.life_is_egg.domain.auth.entity.User;
import com.ohjeon.life_is_egg.domain.auth.repository.UserRepository;
import com.ohjeon.life_is_egg.domain.cheer.entity.Cheer;
import com.ohjeon.life_is_egg.domain.post.entity.Post;
import com.ohjeon.life_is_egg.domain.report.dto.ReportCreateRequest;
import com.ohjeon.life_is_egg.domain.report.entity.Report;
import com.ohjeon.life_is_egg.domain.report.port.CheerLookupPort;
import com.ohjeon.life_is_egg.domain.report.port.PostLookupPort;
import com.ohjeon.life_is_egg.domain.report.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final PostLookupPort postLookupPort;
    private final CheerLookupPort cheerLookupPort;

    public void create(Long userId, ReportCreateRequest request) {
        if (request.getPostId() == null && request.getCheerId() == null) {
            throw new IllegalArgumentException("신고 대상을 선택해주세요.");
        }

        if (request.getPostId() != null && request.getCheerId() != null) {
            throw new IllegalArgumentException("신고 대상은 하나만 선택해주세요.");
        }

        User reporter = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));

        Post post = null;
        Cheer cheer = null;

        if (request.getPostId() != null) {
            post = postLookupPort.getById(request.getPostId());
        }

        if (request.getCheerId() != null) {
            cheer = cheerLookupPort.getById(request.getCheerId());
        }

        Report report = Report.builder()
                .reporter(reporter)
                .post(post)
                .cheer(cheer)
                .reason(request.getReason())
                .build();

        reportRepository.save(report);
    }
}