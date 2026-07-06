package com.feedback.feedback360.dto;

import lombok.*;

//used only by the mock TalentUp seed endpoint — mirrors the real TalentUp contract
//puisque le sync service has no user-facing DTOs this DTO is needed externally since its the seed payload for the endpoint
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class SeedCompletionDTO {
    private UserInfo user;
    private ModuleInfo module;
    private ParcoursInfo parcours;
    private PopulationInfo population;

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class UserInfo { private Long id; private String email; private String fullName; }
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class ModuleTypeInfo { private Long id; private String label; }
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class ModuleInfo { private Long id; private String name; private ModuleTypeInfo type; }
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class ParcoursInfo { private Long id; private String name; }
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class PopulationInfo { private Long id; private String name; }
}