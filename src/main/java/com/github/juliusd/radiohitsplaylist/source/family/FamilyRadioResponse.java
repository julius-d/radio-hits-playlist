package com.github.juliusd.radiohitsplaylist.source.family;

import java.util.List;

record FamilyRadioResponse(int error, List<FamilyRadioTrack> data) {}
