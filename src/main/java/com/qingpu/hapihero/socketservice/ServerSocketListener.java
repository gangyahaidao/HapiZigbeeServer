package com.qingpu.hapihero.socketservice;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.springframework.web.context.support.WebApplicationContextUtils;

import com.qingpu.hapihero.device.dao.ICoordinatorDeviceDao;
import com.qingpu.hapihero.device.dao.IEndDeviceDao;
import com.qingpu.hapihero.device.dao.IRastberryDeviceDao;

/**
 * 主socket进程监听器，随servlet的启动而一起启动
 * */
@WebListener
public class ServerSocketListener implements ServletContextListener {
	
	private ServerSocketThread socketService;
		
	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		// 当服务器关闭servlet上下文时执行此方法
		if(null != socketService && !socketService.isInterrupted()){			
			socketService.closeSocketService();//关闭serversocket
		}
	}
	@Override
	public void contextInitialized(ServletContextEvent sce) {
		// 当servlet上下文被加载时执行此方法
		if(null == socketService){
			ServletContext context = sce.getServletContext();

			IRastberryDeviceDao rastberryDao = WebApplicationContextUtils.getWebApplicationContext(context).getBean(IRastberryDeviceDao.class);
			ICoordinatorDeviceDao coordDao = WebApplicationContextUtils.getWebApplicationContext(context).getBean(ICoordinatorDeviceDao.class);
			IEndDeviceDao endDao = WebApplicationContextUtils.getWebApplicationContext(context).getBean(IEndDeviceDao.class);

			socketService = new ServerSocketThread(rastberryDao, coordDao, endDao); // 启动节点ServerSocket
			//启动主线程
			socketService.start();
        }
	}
}