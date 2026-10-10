package com.example.harness

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.harness.databinding.ActivityLauncherBinding

class LauncherActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityLauncherBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.btnNotes.setOnClickListener { open(NotesListActivity::class.java) }
        binding.btnRichText.setOnClickListener { open(RichTextActivity::class.java) }
        binding.btnAttachments.setOnClickListener { open(AttachmentsActivity::class.java) }
        binding.btnSearch.setOnClickListener { open(SearchActivity::class.java) }
        binding.btnOrganize.setOnClickListener { open(OrganizeActivity::class.java) }
        binding.btnState.setOnClickListener { open(StateActivity::class.java) }
        binding.btnReminders.setOnClickListener { open(RemindersActivity::class.java) }
        binding.btnBackup.setOnClickListener { open(BackupActivity::class.java) }
        binding.btnSettings.setOnClickListener { open(SettingsActivity::class.java) }
        binding.btnSync.setOnClickListener { open(SyncActivity::class.java) }
    }

    private fun open(activityClass: Class<out AppCompatActivity>) {
        startActivity(Intent(this, activityClass))
    }
}
