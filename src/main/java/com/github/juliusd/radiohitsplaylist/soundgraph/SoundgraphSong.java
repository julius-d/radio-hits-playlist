package com.github.juliusd.radiohitsplaylist.soundgraph;

import java.net.URI;
import java.util.List;

public record SoundgraphSong(URI uri, boolean explicit, String title, List<String> artists) {
} 