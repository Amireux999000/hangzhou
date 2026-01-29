package com.example.livedebate.service;

import com.example.livedebate.model.*;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class MockDataService {

    @Autowired
    private WebSocketService webSocketService;

    private Vote currentVotes = new Vote(0, 0, 0, 50, 50);
    private DebateTopic debateTopic;
    private List<AIContent> aiDebateContent = new CopyOnWriteArrayList<>();
    private Map<String, User> users = new ConcurrentHashMap<>();
    private Map<String, Stream> streams = new ConcurrentHashMap<>();
    private LiveStatus globalLiveStatus = new LiveStatus();
    private AIStatus globalAIStatus = new AIStatus("stopped", null, null, new HashMap<>(), new HashMap<>(), null);
    private LiveSchedule liveSchedule = new LiveSchedule();

    @PostConstruct
    public void init() {
        debateTopic = new DebateTopic("debate-default-001", "如果有一个能一键消除痛苦的按钮，你会按吗？", "这是一个关于痛苦、成长与人性选择的深度辩论", "正方", "反方");

        Stream stream1 = new Stream();
        stream1.setId("stream-001");
        stream1.setName("默认直播流");
        stream1.setUrl("http://devimages.apple.com/iphone/samples/bipbop/bipbopall.m3u8");
        stream1.setType("hls");
        stream1.setEnabled(true);
        streams.put(stream1.getId(), stream1);

        addInitialAIContent();
    }

    private void addInitialAIContent() {
        createAIContent("正方观点：痛苦是人生成长的必要经历，消除痛苦会让我们失去学习和成长的机会。", "left", 300000);
        createAIContent("反方观点：如果能够消除痛苦，为什么不呢？痛苦本身没有价值，消除痛苦可以让人更专注于积极的事情。", "right", 240000);
    }

    private void createAIContent(String text, String side, long timeOffset) {
        AIContent content = new AIContent();
        content.setId(UUID.randomUUID().toString());
        content.setDebate_id(debateTopic.getId());
        content.setText(text);
        content.setContent(text);
        content.setSide(side);
        content.setTimestamp(System.currentTimeMillis() - timeOffset);
        content.setLikes(new Random().nextInt(50));
        aiDebateContent.add(content);
    }

    public Vote getVotes() {
        int total = currentVotes.getLeftVotes() + currentVotes.getRightVotes();
        currentVotes.setTotalVotes(total);
        if (total > 0) {
            currentVotes.setLeftPercentage(Math.round((float) currentVotes.getLeftVotes() / total * 100));
            currentVotes.setRightPercentage(Math.round((float) currentVotes.getRightVotes() / total * 100));
        } else {
            currentVotes.setLeftPercentage(50);
            currentVotes.setRightPercentage(50);
        }
        return currentVotes;
    }

    public void vote(String side, int count) {
        if ("left".equals(side)) {
            currentVotes.setLeftVotes(currentVotes.getLeftVotes() + count);
        } else {
            currentVotes.setRightVotes(currentVotes.getRightVotes() + count);
        }
        broadcastVotes();
    }
    
    public void vote100(int left, int right) {
        currentVotes.setLeftVotes(currentVotes.getLeftVotes() + left);
        currentVotes.setRightVotes(currentVotes.getRightVotes() + right);
        broadcastVotes();
    }

    public void resetVotes() {
        currentVotes = new Vote(0, 0, 0, 50, 50);
        broadcastVotes();
    }

    public void updateVotes(int left, int right) {
        currentVotes.setLeftVotes(left);
        currentVotes.setRightVotes(right);
        broadcastVotes();
    }

    public void setAIStatus(boolean running) {
        if (running) {
            globalAIStatus.setStatus("running");
            globalAIStatus.setStartTime(new Date().toString());
        } else {
            globalAIStatus.setStatus("stopped");
            globalAIStatus.setStopTime(new Date().toString());
        }
    }
    
    public List<Comment> getCommentsForContent(String contentId) {
        // Mock comments
        List<Comment> comments = new ArrayList<>();
        comments.add(new Comment("user-1", "User A", "Great point!", System.currentTimeMillis()));
        comments.add(new Comment("user-2", "User B", "I disagree.", System.currentTimeMillis()));
        return comments;
    }
    
    public int getViewers() {
        return new Random().nextInt(500) + 100;
    }
    
    public List<User> getUsers() {
        if (users.isEmpty()) {
            users.put("judge-1", new User("judge-1", "Judge A", "judge", "avatar1.png"));
            users.put("judge-2", new User("judge-2", "Judge B", "judge", "avatar2.png"));
            users.put("user-1", new User("user-1", "User 1", "user", "avatar3.png"));
        }
        return new ArrayList<>(users.values());
    }
    
    public void updateDebate(String id, DebateTopic topic) {
        if (debateTopic.getId().equals(id)) {
            // Preserve ID if not provided in the update
            if (topic.getId() == null) {
                topic.setId(debateTopic.getId());
            }
            this.debateTopic = topic;
        }
    }

    private void broadcastVotes() {
        Vote v = getVotes();
        webSocketService.broadcast("votes-updated", v);
    }

    public DebateTopic getDebateTopic() {
        return debateTopic;
    }

    public List<AIContent> getAiDebateContent() {
        return aiDebateContent;
    }

    public LiveStatus getLiveStatus() {
        Stream active = streams.values().stream().filter(Stream::isEnabled).findFirst().orElse(null);
        if (active != null) {
            globalLiveStatus.setActiveStreamUrl(active.getUrl());
            globalLiveStatus.setActiveStreamId(active.getId());
            globalLiveStatus.setActiveStreamName(active.getName());
        }
        globalLiveStatus.setSchedule(liveSchedule);
        return globalLiveStatus;
    }
    
    public void updateLiveStatus(LiveStatus status) {
        if (status.isLive()) {
            globalLiveStatus.setLive(true);
            globalLiveStatus.setStreamUrl(status.getStreamUrl());
            globalLiveStatus.setStreamId(status.getStreamId());
            globalLiveStatus.setStartTime(new Date().toString());
            webSocketService.broadcast("liveStatus", globalLiveStatus);
        } else {
            globalLiveStatus.setLive(false);
            globalLiveStatus.setStopTime(new Date().toString());
            webSocketService.broadcast("liveStatus", globalLiveStatus);
        }
    }

    public Map<String, Stream> getStreams() {
        return streams;
    }

    public AIStatus getAIStatus() {
        return globalAIStatus;
    }

    @Scheduled(fixedRate = 3000)
    public void simulateVoteChanges() {
        if (!globalLiveStatus.isLive()) return;
        
        Random rand = new Random();
        currentVotes.setLeftVotes(currentVotes.getLeftVotes() + rand.nextInt(5) + 1);
        currentVotes.setRightVotes(currentVotes.getRightVotes() + rand.nextInt(5) + 1);
        broadcastVotes();
    }

    @Scheduled(fixedRate = 15000)
    public void simulateAIContent() {
        if (!globalLiveStatus.isLive()) return;
        
        String[] texts = {
            "正方补充：痛苦让我们珍惜快乐，没有对比就没有真正的幸福。",
            "反方补充：现代医学已经在消除很多痛苦，这个按钮只是技术的延伸。",
            "正方质疑：如果所有人都按这个按钮，社会会变成什么样？",
            "反方回应：每个人都有自己的选择权，不应该强迫别人承受痛苦。"
        };
        String[] sides = {"left", "right", "left", "right"};
        
        int idx = new Random().nextInt(texts.length);
        AIContent content = new AIContent();
        content.setId(UUID.randomUUID().toString());
        content.setDebate_id(debateTopic.getId());
        content.setText(texts[idx]);
        content.setContent(texts[idx]);
        content.setSide(sides[idx]);
        content.setTimestamp(System.currentTimeMillis());
        content.setLikes(new Random().nextInt(10));
        
        aiDebateContent.add(content);
        webSocketService.broadcast("newAIContent", content);
    }
}
