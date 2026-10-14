package l10_flow.practice

import app.cash.turbine.test
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import l10_flow.practice.chat.ChatRoom
import l10_flow.practice.chat.Message
import l10_flow.practice.chat.PostResult
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ChatRoomTest {
    @Test
    fun `a late joiner gets only the last N messages`() = runTest {
        val room = ChatRoom(historySize = 2)
        room.join("alice")
        room.post("alice", "one")
        room.post("alice", "two")
        room.post("alice", "three")
        room.join("bob").test {
            assertEquals(Message("alice", "two"), awaitItem())
            assertEquals(Message("alice", "three"), awaitItem())
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `a feed skips your own messages and ends when you leave`() = runTest {
        val room = ChatRoom(historySize = 0)
        room.join("alice")
        room.join("bob").test {
            room.listeners.first { it == 1 } // bob's feed is subscribed
            room.post("bob", "my own message")
            room.post("alice", "hello bob")
            assertEquals(Message("alice", "hello bob"), awaitItem())
            room.leave("bob")
            awaitComplete()
        }
        assertEquals(setOf("alice"), room.members.value)
    }

    @Test
    fun `posting as a non-member is refused, blank text is a bug`() = runTest {
        val room = ChatRoom()
        assertEquals(PostResult.NotAMember, room.post("carol", "hi"))
        room.join("carol")
        assertEquals(PostResult.Sent, room.post("carol", "hi"))
        assertFailsWith<IllegalArgumentException> { room.post("carol", "   ") }
    }

    @Test
    fun `typing events reach current listeners only`() = runTest {
        val room = ChatRoom()
        room.join("alice")
        room.startTyping("alice") // nobody listening yet: not replayed
        room.typing.test {
            room.typingListeners.first { it == 1 }
            room.startTyping("alice")
            room.startTyping("stranger") // not a member: ignored
            assertEquals("alice", awaitItem())
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `invalid arguments are rejected`() {
        assertFailsWith<IllegalArgumentException> { ChatRoom(historySize = -1) }
        assertFailsWith<IllegalArgumentException> { ChatRoom().join(" ") }
    }
}
