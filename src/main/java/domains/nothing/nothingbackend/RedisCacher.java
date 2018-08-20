package domains.nothing.nothingbackend;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Response;
import redis.clients.jedis.Transaction;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class RedisCacher {
	public static void insertImage(Jedis jedis, DatabaseConnection.UploadedImage img) {
		Transaction multi = jedis.multi();
		String prefix = "image:" + img.url;

		// Hash structure
		multi.set(prefix, img.hash);
		multi.expire(prefix, 300);
		prefix += ':';

		// Type structure
		multi.set(prefix + "type", img.type);
		multi.expire(prefix + "type", 300);

		// Downloading structure
		if (img.download) { // Not needed otherwise
			multi.set(prefix + "filename", img.filename);
			multi.expire(prefix + "filename", 300);
		}

		// Deletion structure
		if (img.deleted != null) {
			multi.set(prefix + "deleted", img.deleted.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
			multi.expire(prefix + "deleted", 300);
		}

		// Uploader structure
		multi.set(prefix + "uploader", img.uploader);
		multi.expire(prefix + "uploader", 300);

		multi.exec();
	}

	public static DatabaseConnection.UploadedImage getImage(Jedis jedis, String url) {
		DatabaseConnection.UploadedImage img = new DatabaseConnection.UploadedImage();

		Transaction multi = jedis.multi();
		String prefix = "image:" + url;
		Response<String> hash = multi.get(prefix);
		Response<Boolean> exists = multi.exists(prefix);
		prefix += ':';
		Response<String> ts = multi.get(prefix + "ts");
		Response<String> deleted = multi.get(prefix + "deleted");
		Response<String> filename = multi.get(prefix + "filename");
		Response<String> type = multi.get(prefix + "type");
		Response<String> uploader = multi.get(prefix + "uploader");
		multi.exec();

		if (!exists.get()) return null;

		img.url = url;
		img.hash = hash.get();
		String tms = ts.get();
		if (tms != null)
			img.ts = LocalDateTime.parse(tms);
		String del = deleted.get();
		if (del != null)
			img.deleted = LocalDateTime.parse(del);
		String fn = filename.get();
		if (fn != null) {
			img.filename = fn;
			img.download = true;
		}
		img.type = type.get();
		img.uploader = uploader.get();
		if (img.uploader == null && exists.get())
			img.exists = false;

		return img;
	}

	public static void expire(Jedis jedis, String url) {
		String prefix = "image:" + url;
		jedis.del(prefix);
		prefix += ':';
		jedis.del(prefix + "ts");
		jedis.del(prefix + "deleted");
		jedis.del(prefix + "filename");
		jedis.del(prefix + "type");
		jedis.del(prefix + "uploader");
	}

	public static void insertNullImage(Jedis j, String image) {
		Transaction multi = j.multi();
		multi.set("image:" + image, "");
		multi.expire("image:" + image, 300);
		multi.exec();
	}
}
