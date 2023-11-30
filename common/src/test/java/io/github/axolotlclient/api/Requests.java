package io.github.axolotlclient.api;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import io.github.axolotlclient.api.requests.ServerRequest;
import io.github.axolotlclient.api.requests.ServerResponse;
import io.github.axolotlclient.api.requests.c2s.*;
import io.github.axolotlclient.api.requests.s2c.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter(AccessLevel.PRIVATE)
public enum Requests {

	HANDSHAKE(0x01, HandshakeC2S.class, HandshakeS2C.class),
	GLOBAL_DATA(0x02, GlobalDataC2S.class, GlobalDataS2C.class),
	FRIENDS_LIST(0x03, FriendsListC2S.class, FriendListS2C.class),
	GET_FRIEND(0x04, GetFriendC2S.class, GetFriendS2C.class),
	USER(0x05, UserC2S.class, GetUserS2C.class),
	CREATE_FRIEND_REQUEST(0x06, CreateFriendRequestC2S.class, CreateFriendRequestS2C.class),
	FRIEND_REQUEST_REACTION(0x07, FriendRequestReactionC2S.class, FriendRequestReactionS2C.class),
	GET_FRIEND_REQUESTS(0x08, GetFriendRequestC2S.class, GetFriendRequestsS2C.class),
	REMOVE_FRIEND(0x09, RemoveFriendC2S.class, null),
	INCOMING_FRIEND_REQUEST(0x0A, IncomingFriendRequestC2S.class, IncomingFriendRequestS2C.class),
	STATUS_UPDATE(0x0B, StatusUpdateC2S.class, StatusUpdateS2C.class),
	CREATE_CHANNEL(0x0C, CreateChannelC2S.class, null),
	GET_OR_CREATE_CHANNEL(0x0D, GetOrCreateChannelC2S.class, GetOrCreateChannelS2C.class),
	GET_MESSAGES(0x0E, GetMessagesC2S.class, GetMessagesS2C.class),
	GET_CHANNEL_LIST(0x0F, GetChannelListC2S.class, GetChannelListS2C.class),
	SEND_MESSAGE(0x10, SendMessageC2S.class, SendMessageS2C.class),
	GET_CHANNEL_BY_ID(0x11, GetChannelByIdC2S.class, GetChannelByIdS2C.class),
	GET_PUBLIC_KEY(0x12, GetPublicKeyC2S.class, GetPublicKeyS2C.class),
	GET_HYPIXEL_API_DATA(0x13, GetHypixelApiDataC2S.class, GetHypixelApiDataS2C.class),
	GET_BLOCKED(0x14, GetBlockedC2S.class, GetBlockedS2C.class),
	BLOCK_USER(0x15, BlockUserC2S.class, BlockUserS2C.class),
	UNBLOCK_USER(0x16, UnblockUserC2S.class, UnblockUserS2C.class),
	UPLOAD_SCREENSHOT(0x17, UploadScreenshotC2S.class, UploadScreenshotS2C.class),
	DOWNLOAD_SCREENSHOT(0x18, DownloadScreenshotC2S.class, DownloadScreenshotS2C.class),
	REPORT_MESSAGE(0x19, ReportMessageC2S.class, null),
	REPORT_USER(0x1A, ReportUserC2S.class, null),
	QUERY_PLURALKIT_INFO(0x1B, QueryPkInformationC2S.class, QueryPkInformationS2C.class),
	UPDATE_PLURALKIT_INFO(0x1C, UpdatePkInformationC2S.class, null),
	ERROR(0xFF, null, ErrorS2C.class);

	private final int type;
	private final Class<? extends ServerRequest> c2s;
	private final Class<? extends ServerResponse> s2c;

	private static final Map<Integer, Class<? extends ServerRequest>> requestsMap = Arrays.stream(values()).filter(r -> r.getC2s() != null).collect(Collectors.toMap(Requests::getType, Requests::getC2s));
	private static final Map<Class<? extends ServerResponse>, Integer> typeToServerPacketMap = Arrays.stream(values()).filter(r -> r.getS2c() != null).collect(Collectors.toMap(Requests::getS2c, Requests::getType));

	public static Class<? extends ServerRequest> fromType(int type){
		return requestsMap.get(type);
	}

	public static int getType(Class<? extends ServerResponse> c){
		return typeToServerPacketMap.get(c);
	}
}
