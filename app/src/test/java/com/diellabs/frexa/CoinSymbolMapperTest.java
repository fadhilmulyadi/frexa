package com.diellabs.frexa;

import com.diellabs.frexa.data.remote.api.CoinSymbolMapper;
import org.junit.Test;
import static org.junit.Assert.*;

public class CoinSymbolMapperTest {

    @Test
    public void toBitfinexSymbol_knownCoin_returnsSymbol() {
        assertEquals("tBTCUSD",  CoinSymbolMapper.toBitfinexSymbol("bitcoin"));
        assertEquals("tETHUSD",  CoinSymbolMapper.toBitfinexSymbol("ethereum"));
        assertEquals("tBNBUSD",  CoinSymbolMapper.toBitfinexSymbol("binancecoin"));
        assertEquals("tSOLUSD",  CoinSymbolMapper.toBitfinexSymbol("solana"));
    }

    @Test
    public void toBitfinexSymbol_unknownCoin_returnsFallback() {
        assertEquals("tNEWCOINUSD", CoinSymbolMapper.toBitfinexSymbol("new-coin"));
    }

    @Test
    public void toBitfinexTimeframe_returnsCorrectTimeframe() {
        assertEquals("1m",  CoinSymbolMapper.toBitfinexTimeframe(60));
        assertEquals("5m",  CoinSymbolMapper.toBitfinexTimeframe(300));
        assertEquals("15m", CoinSymbolMapper.toBitfinexTimeframe(900));
        assertEquals("1h",  CoinSymbolMapper.toBitfinexTimeframe(3600));
        assertEquals("1D",  CoinSymbolMapper.toBitfinexTimeframe(86400));
    }
}
