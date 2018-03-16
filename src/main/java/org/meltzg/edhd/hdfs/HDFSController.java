package org.meltzg.edhd.hdfs;

import java.io.IOException;
import java.net.URLDecoder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HDFSController {

	@Autowired
	private IHDFSService hdfsService;
	
	@RequestMapping("/hdfs-ls/{path}")
	public HDFSLocationInfo getChildren(@PathVariable String path) {
		try {
			path = URLDecoder.decode(path, "UTF-8");
			return hdfsService.getChildren(path);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
