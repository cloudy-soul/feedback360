package com.feedback.feedback360.services.talentup;

public record TalentUpCompletionDto(
    TalentUpUserDto user,
    TalentUpModuleDto module,
    TalentUpParcoursDto parcours,
    TalentUpPopulationDto population
) {}