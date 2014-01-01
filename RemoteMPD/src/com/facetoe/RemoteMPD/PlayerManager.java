package com.facetoe.RemoteMPD;

abstract class PlayerManager implements PlayerController {
    RemoteMPDCommandService commandService;

    public PlayerManager(RemoteMPDCommandService service) {
        commandService = service;
    }
}