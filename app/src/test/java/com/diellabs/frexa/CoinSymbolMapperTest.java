package com.diellabs.frexa;

import com.diellabs.frexa.data.remote.api.CoinSymbolMapper;
import org.junit.Test;
import static org.junit.Assert.*;

public class CoinSymbolMapperTest {

    @Test
    public void toBitgetSymbol_knownCoin_returnsSymbol() {
        assertEquals("BTCUSDT",  CoinSymbolMapper.toBitgetSymbol("bitcoin"));
        assertEquals("ETHUSDT",  CoinSymbolMapper.toBitgetSymbol("ethereum"));
        assertEquals("BNBUSDT",  CoinSymbolMapper.toBitgetSymbol("binancecoin"));
        assertEquals("SOLUSDT",  CoinSymbolMapper.toBitgetSymbol("solana"));
    }

    @Test
    public void toBitgetSymbol_unknownCoin_returnsFallback() {
        assertEquals("NEWCOINUSDT", CoinSymbolMapper.toBitgetSymbol("new-coin"));
    }

    @Test
    public void toBitgetGranularity_returnsCorrectGranularity() {
        assertEquals("1min",  CoinSymbolMapper.toBitgetGranularity(60));
        assertEquals("5min",  CoinSymbolMapper.toBitgetGranularity(300));
        assertEquals("15min", CoinSymbolMapper.toBitgetGranularity(900));
        assertEquals("1h",    CoinSymbolMapper.toBitgetGranularity(3600));
        assertEquals("4h",    CoinSymbolMapper.toBitgetGranularity(14400));
        assertEquals("1day",  CoinSymbolMapper.toBitgetGranularity(86400));
    }
}
