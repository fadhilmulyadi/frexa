package com.diellabs.frexa;

import com.diellabs.frexa.data.remote.api.CoinSymbolMapper;
import org.junit.Test;
import static org.junit.Assert.*;

public class CoinSymbolMapperTest {

    @Test
    public void toCryptoCompareFsym_knownCoin_returnsFsym() {
        assertEquals("BTC",  CoinSymbolMapper.toCryptoCompareFsym("bitcoin"));
        assertEquals("ETH",  CoinSymbolMapper.toCryptoCompareFsym("ethereum"));
        assertEquals("BNB",  CoinSymbolMapper.toCryptoCompareFsym("binancecoin"));
        assertEquals("SOL",  CoinSymbolMapper.toCryptoCompareFsym("solana"));
        assertEquals("DOGE", CoinSymbolMapper.toCryptoCompareFsym("dogecoin"));
    }

    @Test
    public void toCryptoCompareFsym_unknownCoin_returnsFallback() {
        assertEquals("NEWCOIN", CoinSymbolMapper.toCryptoCompareFsym("new-coin"));
    }

    @Test
    public void toAggregate_returnsCorrectValue() {
        assertEquals(1,  CoinSymbolMapper.toAggregate(60));
        assertEquals(5,  CoinSymbolMapper.toAggregate(300));
        assertEquals(15, CoinSymbolMapper.toAggregate(900));
        assertEquals(30, CoinSymbolMapper.toAggregate(1800));
        assertEquals(1,  CoinSymbolMapper.toAggregate(3600));
    }

    @Test
    public void isHourly_returnsTrueOnlyFor3600() {
        assertFalse(CoinSymbolMapper.isHourly(60));
        assertFalse(CoinSymbolMapper.isHourly(1800));
        assertTrue(CoinSymbolMapper.isHourly(3600));
    }
}
