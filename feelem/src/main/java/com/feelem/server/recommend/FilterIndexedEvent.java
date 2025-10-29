package com.feelem.server.recommend;

import com.feelem.server.recommend.dto.IndexFilterRequest;

public record FilterIndexedEvent(IndexFilterRequest payload) {}
