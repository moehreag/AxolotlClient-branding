/*
 * Copyright © 2024 moehreag <moehreag@gmail.com> & Contributors
 *
 * This file is part of AxolotlClient.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 * For more information, see the LICENSE file.
 */

package io.github.axolotlclient.api.worldhost;

import java.util.*;
import java.util.function.Consumer;

import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import com.mojang.serialization.JsonOps;
import com.mojang.util.UndashedUuid;
import io.github.axolotlclient.api.API;
import io.github.axolotlclient.api.handlers.StatusUpdateHandler;
import io.github.axolotlclient.api.requests.FriendRequest;
import io.github.axolotlclient.api.requests.UserRequest;
import io.github.axolotlclient.api.types.User;
import io.github.axolotlclient.api.util.UUIDHelper;
import io.github.axolotlclient.util.GsonHelper;
import io.github.gaming32.worldhost.FriendsListUpdate;
import io.github.gaming32.worldhost.WorldHost;
import io.github.gaming32.worldhost.gui.screen.PlayerInfoScreen;
import io.github.gaming32.worldhost.plugin.*;
import io.github.gaming32.worldhost.plugin.vanilla.GameProfileBasedProfilable;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.server.ServerMetadata;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

public class AxolotlClientWorldHostPlugin implements WorldHostPlugin {

    static AxolotlClientWorldHostPlugin Instance;
    private final FriendAdder friendAdder = new AxolotlClientFriendAdder();

    public AxolotlClientWorldHostPlugin() {
        Instance = this;
        API.addStartupListener(() -> WorldHost.reconnect(false, true));
        StatusUpdateHandler.addUpdateListener(user -> {
            if (user.getStatus().isOnline() && user.getStatus().getActivity() != null) {
                if (user.getStatus().getActivity().title().equals("api.status.title.world_host")) {
                    AxolotlClientOnlineFriend friend = AxolotlClientOnlineFriend.of(user);
                    WorldHost.ONLINE_FRIENDS.put(friend.uuid(), friend);
                    WorldHost.ONLINE_FRIEND_UPDATES.forEach(FriendsListUpdate::friendsListUpdate);
                }
            }
        });
    }

    String getWhStatusDescription() {
        Map<String, Object> fields = new HashMap<>();
        fields.put("value", MinecraftClient.getInstance().getServer().getSaveProperties().getWorldName());
        if (MinecraftClient.getInstance().getServer().isRemote()) {
            fields.put("connection_id", WorldHost.CONNECTION_ID);
            fields.put("server_metadata", ServerMetadata.CODEC.encodeStart(JsonOps.INSTANCE, MinecraftClient.getInstance().getServer().getServerMetadata()).getOrThrow());
        }
        return GsonHelper.GSON.toJson(fields);
    }

    @Override
    public void listFriends(Consumer<FriendListFriend> friendConsumer) {
        FriendRequest.getInstance().getFriends().thenAccept(list -> {
            list.stream().map(AxolotlClientFriendListFriend::new).forEach(friendConsumer);
        });
    }

    @Override
    public Optional<FriendAdder> friendAdder() {
        return Optional.of(friendAdder);
    }

    @Override
    public void pingFriends(Collection<OnlineFriend> friends) {
        friends.stream().filter(AxolotlClientOnlineFriend.class::isInstance).forEach(friend -> {
            WorldHost.ONLINE_FRIEND_PINGS.put(friend.uuid(), AxolotlClientUserInfo.parse(((AxolotlClientOnlineFriend) friend).user.getStatus().getActivity().rawDescription()).metadata());
        });
    }

    @Override
    public void refreshOnlineFriends() {
        FriendRequest.getInstance().getFriends().thenAccept(list -> {
            list.stream().filter(u -> u.getStatus().getTitle().equals("api.status.title.world_host")).map(AxolotlClientFriendListFriend::new).forEach(friend -> {
                WorldHost.ONLINE_FRIENDS.put(friend.profile.getId(), AxolotlClientOnlineFriend.of(friend.friend));
            });
            WorldHost.ONLINE_FRIEND_UPDATES.forEach(FriendsListUpdate::friendsListUpdate);
        });
    }

    private record AxolotlClientUserInfo(long connectionId, @Nullable ServerMetadata metadata) {
        public static AxolotlClientUserInfo parse(String json) {
            JsonObject map = GsonHelper.fromJson(json);
            long connectionId = map.has("connection_id") ? map.get("connection_id").getAsLong() : -1;
            ServerMetadata metadata = map.has("server_metadata") ? ServerMetadata.CODEC.parse(JsonOps.INSTANCE, map.get("server_metadata")).getOrThrow() : null;
            return new AxolotlClientUserInfo(connectionId, metadata);
        }
    }

    private record AxolotlClientOnlineFriend(User user, GameProfile profile,
                                             long connectionId) implements OnlineFriend, GameProfileBasedProfilable {
        private static AxolotlClientOnlineFriend of(User user) {
            if (user.getStatus().isOnline() && user.getStatus().getActivity() != null) {
                if (user.getStatus().getActivity().title().equals("api.status.title.world_host")) {
                    String data = user.getStatus().getActivity().rawDescription();
                    long connectionId = AxolotlClientUserInfo.parse(data).connectionId();
                    return new AxolotlClientOnlineFriend(user, new GameProfile(UndashedUuid.fromStringLenient(user.getUuid()), user.getName()), connectionId);

                }
            }
            throw new IllegalArgumentException();
        }

        @Override
        public UUID uuid() {
            return profile.getId();
        }

        @Override
        public void joinWorld(Screen screen) {
            WorldHost.join(connectionId, screen);
        }

        @Override
        public GameProfile defaultProfile() {
            return profile;
        }

        @Override
        public Joinability joinability() {
            return connectionId == -1 ? new Joinability.Unjoinable(Text.translatable("api.worldhost.joinability.not_published")) : Joinability.Joinable.INSTANCE;
        }
    }

    private record AxolotlClientFriendListFriend(User friend,
                                                 GameProfile profile) implements FriendListFriend, GameProfileBasedProfilable {
        private AxolotlClientFriendListFriend(User friend) {
            this(friend, new GameProfile(UndashedUuid.fromStringLenient(friend.getUuid()), friend.getName()));
        }

        @Override
        public void removeFriend(Runnable runnable) {
            FriendRequest.getInstance().removeFriend(friend);
        }

        @Override
        public void showFriendInfo(Screen screen) {
            MinecraftClient.getInstance().setScreen(new PlayerInfoScreen(screen, profile));
        }

        @Override
        public GameProfile defaultProfile() {
            return profile;
        }

        @Override
        public void addFriend(boolean notify, Runnable refresher) {
            FriendRequest.getInstance().addFriend(friend.getUuid());
            refresher.run();
        }

        @Override
        public Optional<Text> tag() {
            return Optional.of(Text.literal("AxolotlClient"));
        }
    }

    private static class AxolotlClientFriendAdder implements FriendAdder {
        @Override
        public Text label() {
            return Text.literal("AxolotlClient");
        }

        @Override
        public void searchFriends(String s, int i, Consumer<FriendListFriend> consumer) {
            if (s.isEmpty()) {
                return;
            }
            Optional<String> uuidOpt = UUIDHelper.ensureUuidOpt(s);
            uuidOpt.ifPresent(uuid -> UserRequest.get(uuid).thenAccept(o -> o.map(AxolotlClientFriendListFriend::new).ifPresent(consumer)));
        }

        @Override
        public boolean delayLookup(String s) {
            return true;
        }

        @Override
        public int maxValidNameLength() {
            return 36;
        }
    }
}
