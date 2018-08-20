package domains.nothing.nothingbackend;

import redis.clients.jedis.Jedis;

public class IPFiltering {
	public static boolean isIpBanned(Jedis jedis, String ip) {
		return jedis.exists("ipban:" + ip);
	}

	public static void ban(Jedis jedis, String ip, String s) {
		String key = "ipban:" + ip;
		jedis.set(key, s);
		jedis.expire(key, 30 * 24 * 3600);
	}
}