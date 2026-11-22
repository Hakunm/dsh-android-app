package io.github.hakunm.deepseekharness

import io.github.hakunm.deepseekharness.data.DshApiException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DirectoryAccessErrorTest {
    @Test
    fun detectsSystemDriveRootsAcrossWindowsAndPosix() {
        assertTrue(isSystemDriveRoot("C:\\"))
        assertTrue(isSystemDriveRoot("d:/"))
        assertTrue(isSystemDriveRoot("E:"))
        assertTrue(isSystemDriveRoot("/"))
        assertFalse(isSystemDriveRoot("C:\\Users\\nyanya"))
        assertFalse(isSystemDriveRoot("/Users/nyanya"))
        assertFalse(isSystemDriveRoot("workspace"))
    }

    @Test
    fun classifiesProtectedDriveRootFailure() {
        val error = DshApiException(500, "INTERNAL_ERROR", "An internal server error occurred.")
        assertEquals(
            "PROTECTED_DRIVE_ROOT|C:\\",
            classifyDirectoryAccessError("C:\\", "", error),
        )
        assertEquals(
            "PROTECTED_DRIVE_ROOT|/",
            classifyDirectoryAccessError("/", "", error),
        )
    }

    @Test
    fun classifiesProtectedSubdirectoryAndCustomAliasRoots() {
        val internalError = DshApiException(500, "INTERNAL_ERROR", "An internal server error occurred.")
        assertEquals(
            "PROTECTED_DIRECTORY|C:\\Users/Default User",
            classifyDirectoryAccessError("C:\\Users", "Default User", internalError),
        )
        assertEquals(
            "PROTECTED_DIRECTORY|SystemDisk",
            classifyDirectoryAccessError("SystemDisk", "", internalError),
        )
    }

    @Test
    fun classifiesSymlinkTraversalAndMissingFolders() {
        val linkError = DshApiException(403, "LINK_TRAVERSAL_FORBIDDEN", "Symbolic links and junctions cannot be traversed.")
        assertEquals(
            "LINK_TRAVERSAL_FORBIDDEN|C:\\Users\\nyanya/Application Data",
            classifyDirectoryAccessError("C:\\Users\\nyanya", "Application Data", linkError),
        )

        val missingError = DshApiException(404, "PATH_NOT_FOUND", "The requested path does not exist.")
        assertEquals(
            "DIRECTORY_NOT_FOUND|project/deleted",
            classifyDirectoryAccessError("project", "deleted", missingError),
        )
    }
}
