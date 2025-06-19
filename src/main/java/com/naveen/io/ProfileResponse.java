package com.naveen.io;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileResponse {
    private String userId; // I think this should be Long
    private String name;
    private String email;
    private Boolean isAccountVerified;
}
