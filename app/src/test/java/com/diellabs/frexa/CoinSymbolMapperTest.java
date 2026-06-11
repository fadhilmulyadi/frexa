package com.diellabs.frexa;

import com.diellabs.frexa.data.remote.api.CoinSymbolMapper;
import org.junit.Test;
import static org.junit.Assert.*;

public class CoinSymbolMapperTest {

    @Test
    public void toMexcSymbol_knownCoin_returnsSymbol() {
        assertEquals("BTCUSDT",  CoinSymbolMapper.toMexcSymbol("bitcoin"));
        assertEquals("ETHUSDT",  CoinSymbolMapper.toMexcSymbol("ethereum"));
        assertEquals("BNBUSDT",  CoinSymbolMapper.toMexcSymbol("binancecoin"));
        assertEquals("SOLUSDT",  CoinSymbolMapper.toMexcSymbol("solana"));
    }

    @Test
    public void toMexcSymbol_unknownCoin_returnsFallback() {
        assertEquals("NEWCOINUSDT", CoinSymbolMapper.toMexcSymbol("new-coin"));
    }

    @Test
    public void toMexcInterval_returnsCorrectInterval() {
        assertEquals("1m",  CoinSymbolMapper.toMexcInterval(60));
        assertEquals("5m",  CoinSymbolMapper.toMexcInterval(300));
        assertEquals("15m", CoinSymbolMapper.toMexcInterval(900));
        assertEquals("1h",  CoinSymbolMapper.toMexcInterval(3600));
        assertEquals("4h",  CoinSymbolMapper.toMexcInterval(14400));
        assertEquals("1d",  CoinSymbolMapper.toMexcInterval(86400));
    }
}
