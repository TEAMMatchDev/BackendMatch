package com.example.matchapi.donation.dto;

import com.example.matchdomain.donation.entity.DonationStatus;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

public class DonationRes {
    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DonationList {
        private Long donationId;

        private String donationStatus;

        private String flameName;

        private String regularStatus;
    }

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class FlameList {
        private Long donationId;

        private String flameName;

        private String donationStatus;

        private Long amount;

        private String createdAt;
    }
}
