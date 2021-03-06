/*
Copyright 2016 Jacopo Rigoli

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package it.polimi.diceH2020.SPACE4CloudWS.fineGrainedLogicForOptimization;

import it.polimi.diceH2020.SPACE4CloudWS.main.DS4CSettings;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import reactor.bus.Event;
import reactor.bus.EventBus;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;


public class QueueHandler<T>{

	private final Logger logger = Logger.getLogger(QueueHandler.class.getName());

	@Autowired
	private EventBus eventBus;

	@Autowired
	private ApplicationContext context;

	@Autowired
	private DS4CSettings settings;


	protected final Object qLock = new Object();

	private List<ChannelInfo> channelsInfoList;  //ChannelState local information about consumer's status.
	protected List<T> jobsQueue;

	@PostConstruct
	private void setUpEnvironment(){
		createChannels();
	}

	public void createChannels(){
		channelsInfoList = new ArrayList<ChannelInfo>();
		jobsQueue = new ArrayList<>();

		for(int i=0; i < settings.getAvailableCores()-1; i++){
			channelsInfoList.add( new ChannelInfo((ReactorConsumer)context.getBean("reactorConsumer",i)));
		}
	}

	public void enqueueJob(T job){
		jobsQueue.add(job);
		printStatus();
		sendJobsToFreeChannels();
	}

	public synchronized void sendJobsToFreeChannels(){ //TODO synchronized?
		channelsInfoList.stream().filter(channelInfo -> channelInfo.getState().equals(States.READY)).forEach(channelInfo->sendJob(channelInfo));
	}

	public synchronized void notifyReadyChannel(ReactorConsumer consumer){
		channelsInfoList.stream().filter(channelInfo -> channelInfo.getConsumer().equals(consumer)).findFirst().get().setState(States.READY);
		//System.out.println(channelsInfoList.stream().filter(channelInfo -> channelInfo.getConsumer().equals(consumer)).findFirst().get().getConsumer().getId() + " is ready");
		printStatus();
		sendJobsToFreeChannels();
	}

	public synchronized void sendJob(ChannelInfo info){
		if(!jobsQueue.isEmpty() && info.getState().equals(States.READY)){
			String channel = "channel"+ info.getConsumer().getId();
			info.setState(States.BUSY);
			eventBus.notify(channel, Event.wrap(jobsQueue.remove(0)));
			logger.info("|Q-STATUS| job sent to " + channel );
		}
	}

	private void printStatus(){
		String queueStatus ="|Q-STATUS|";
		queueStatus += " (# jobs in Q: "+jobsQueue.size()+") ";
		for(int i=0; i<channelsInfoList.size();i++){
			queueStatus += i + ": ";
			queueStatus += channelsInfoList.get(i).getState();
			queueStatus += (" || ");
		}
		logger.info(queueStatus+"\n");
	}

}
