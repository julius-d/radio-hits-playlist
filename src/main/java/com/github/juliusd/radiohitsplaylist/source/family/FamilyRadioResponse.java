package com.github.juliusd.radiohitsplaylist.source.family;

import java.util.List;

record FamilyRadioResponse(int size, List<FamilyRadioTrackWrapper> items, String next) {}
