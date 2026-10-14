package l10_flow.practice.chat

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * =====================================================================
 *  PRACTICE 10.C — A chat room
 * =====================================================================
 *  GOAL   : Combine the hot flows of this level: a SharedFlow of
 *           messages with replayed history, a StateFlow of members, a
 *           SharedFlow of typing events, and a per-user feed that ends
 *           when the user leaves.
 *  RUN    : ./gradlew -q run -Plesson=l10_flow.practice.chat.ChatRoomKt
 *  OUTPUT :
 *           members: [alice, bob]
 *           bob got (history): alice: hi everyone
 *           bob got: alice: welcome, bob!
 *           alice saw typing: bob
 *           post by carol: NotAMember
 *           bob's feed ended after leaving: true
 *           members: [alice]
 *  DESIGN : Messages are a MutableSharedFlow with replay = historySize,
 *           so a newcomer first gets the recent history. members and
 *           typing are exposed read-only (asStateFlow / asSharedFlow).
 *           join() returns a channelFlow that forwards messages until the
 *           user is no longer in members, then completes. Posting as a
 *           non-member is a normal refusal (a result value); a blank
 *           text is a caller bug (require).
 *           Kept simple on purpose: membership is a Set of names, so if
 *           a user leaves and joins again before the old feed notices,
 *           the old feed stays open. A real app would give each join its
 *           own session id.
 *  EXTEND : Add private messages: post(to = "bob") is only delivered to bob.
 * =====================================================================
 */

const val DEFAULT_HISTORY = 10
private const val EVENT_BUFFER = 64

data class Message(val from: String, val text: String) {
    override fun toString() = "$from: $text"
}

sealed interface PostResult {
    data object Sent : PostResult
    data object NotAMember : PostResult
}

class ChatRoom(historySize: Int = DEFAULT_HISTORY) {
    init {
        require(historySize >= 0) { "historySize must not be negative, was $historySize" }
    }

    // extraBufferCapacity: a slow reader doesn't make post() wait at once.
    private val messages = MutableSharedFlow<Message>(replay = historySize, extraBufferCapacity = EVENT_BUFFER)
    private val _members = MutableStateFlow<Set<String>>(emptySet())
    private val _typing = MutableSharedFlow<String>(extraBufferCapacity = EVENT_BUFFER)

    val members: StateFlow<Set<String>> = _members.asStateFlow()

    /** "someone is typing" events: not replayed, only for current listeners. */
    val typing: SharedFlow<String> = _typing.asSharedFlow()

    /** How many feeds are listening right now (useful for handshakes in tests). */
    val listeners: StateFlow<Int> = messages.subscriptionCount

    /** How many collectors watch [typing] right now. */
    val typingListeners: StateFlow<Int> = _typing.subscriptionCount

    /**
     * Adds [user] to the room and returns their feed: recent history first,
     * then new messages from OTHER users. The feed completes when the user leaves.
     */
    fun join(user: String): Flow<Message> {
        require(user.isNotBlank()) { "user must not be blank" }
        _members.update { it + user }
        return channelFlow {
            val forwarding = launch {
                messages.collect { if (it.from != user) send(it) }
            }
            members.first { user !in it } // suspend until the user leaves
            forwarding.cancel()
        }
    }

    fun leave(user: String) = _members.update { it - user }

    suspend fun post(user: String, text: String): PostResult {
        require(text.isNotBlank()) { "text must not be blank" }
        if (user !in members.value) return PostResult.NotAMember
        messages.emit(Message(user, text))
        return PostResult.Sent
    }

    fun startTyping(user: String) {
        if (user in members.value) _typing.tryEmit(user)
    }
}

fun main() =
    runBlocking {
        val room = ChatRoom(historySize = 1)
        room.join("alice") // alice is a member; this demo doesn't read her feed
        room.post("alice", "hi everyone") // before bob is here: kept as history

        // bob's feed goes into an inbox channel so main can wait for each message.
        val bobInbox = Channel<Message>(Channel.UNLIMITED)
        val bobFeed = room.join("bob")
        println("members: ${room.members.value.toList()}")
        val bobListening = launch { bobFeed.collect { bobInbox.send(it) } }
        println("bob got (history): ${bobInbox.receive()}")

        room.post("alice", "welcome, bob!")
        println("bob got: ${bobInbox.receive()}")

        val typingSeen = Channel<String>(Channel.UNLIMITED)
        val aliceWatching = launch { room.typing.collect { typingSeen.send(it) } }
        room.typingListeners.first { it == 1 } // handshake before the event
        room.startTyping("bob")
        println("alice saw typing: ${typingSeen.receive()}")
        aliceWatching.cancel()

        println("post by carol: ${room.post("carol", "can I join?")}")

        room.leave("bob")
        bobListening.join() // returns only because bob's feed completed
        println("bob's feed ended after leaving: ${bobListening.isCompleted && !bobListening.isCancelled}")
        println("members: ${room.members.value.toList()}")
    }
