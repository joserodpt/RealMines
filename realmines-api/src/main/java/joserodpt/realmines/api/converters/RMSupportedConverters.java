package joserodpt.realmines.api.converters;

/*
 *  ______           ____  ____
 *  | ___ \         | |  \/  (_)
 *  | |_/ /___  __ _| | .  . |_ _ __   ___  ___
 *  |    // _ \/ _` | | |\/| | | '_ \ / _ \/ __|
 *  | |\ \  __/ (_| | | |  | | | | | |  __/\__ \
 *  \_| \_\___|\__,_|_\_|  |_/_|_| |_|\___||___/
 *
 * Licensed under the MIT License
 * @author José Rodrigues © 2019-2024
 * @link https://github.com/joserodpt/RealMines
 */

import joserodpt.realmines.api.RealMinesAPI;

public enum RMSupportedConverters {

    CATA_MINES("CataMines"),
    JETS_PRISON_MINES("JetsPrisonMines"),
    MINE_RESET_LITE("MineResetLite");

    private final String source;

    RMSupportedConverters(String source) {
        this.source = source;
    }

    public String getSourceName() {
        return this.source;
    }

    public RMConverterBase getConverter(RealMinesAPI api) {
        switch (this) {
            case CATA_MINES:
                return new CataMinesConverter(api);
            case MINE_RESET_LITE:
                return new MRLConverter(api);
            case JETS_PRISON_MINES:
                return new JetsPrisonMinesConverter(api);
        }
        return null;
    }
}
