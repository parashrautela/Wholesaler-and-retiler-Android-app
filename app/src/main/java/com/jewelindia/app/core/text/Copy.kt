// Port of: wholesaler ios/JewelIndia/Sources/Core/Copy.swift
package com.jewelindia.app.core.text

/**
 * Every user-facing string in the auth + shell surface, copied verbatim from
 * the web app. Centralising them keeps the parity pass mechanical.
 */
object Copy {

    /// Every user-facing string in the auth + shell surface, copied verbatim from
    /// the web app. Centralising them keeps the parity pass mechanical: a diff
    /// against the web source is a diff against this file.
    ///
    /// Punctuation is significant and intentional — the web mixes `...` and `…`,
    /// uses en-dashes in the password rules, and ends the same sentence with and
    /// without a period on different screens. All of that is preserved.

        // MARK: - Shared chrome (AuthLayout)

    val brandMark = "JI"
    val brandWordmark = "Jewels India"
    val legal = "By continuing, you agree to our "
    val legalTerms = "Terms of Service"
    val legalAnd = " and "
    val legalPrivacy = "Privacy\u00A0Policy"
    val legalSupportLead = "Need help? "
    val legalSupport = "Contact support"

        // MARK: - Entry screen · /entry_page/signup

    val entryHeading = "Welcome"
    val entrySubheading = "Sign in to explore Jewellery all over India"
    val entryFieldLabel = "Email or Phone number"
    val entryFieldPlaceholder = "Enter"
    val entryDivider = "OR"
    val entryGoogleIdle = "Continue with Google"
    val entryGoogleBusy = "Signing in..."
    val entrySubmitIdle = "Continue"
    val entrySubmitBusy = "Checking..."

    val bannedError = "Your account has been banned. Please use a different number or email."
    val invalidMobile = "Please enter a valid 10-digit Indian mobile number."
    val genericFailure = "Something went wrong. Please try again."
    val otpSendFailure = "Failed to send OTP. Please try again."
    val networkError = "Network error. Please check your connection and try again."
        /// Shown in the red error slot even though it is a success path — matching the web.
    val googleRedirect = "You signed up with Google. Redirecting..."

        /// Not a web string. Google sign-in returns to the app via
        /// `jewelindia://auth/callback`, which must be listed under Supabase →
        /// Authentication → URL Configuration → Redirect URLs. Without it Supabase
        /// redirects to the website instead and the app never receives a session.
    val googleRedirectNotConfigured = "Couldn't complete Google sign-in. Please try again or contact support."

        // MARK: - Sign in · /entry_page/signin

    val signInHeading = "Welcome"
    val signInSubheading = "Sign in to explore Jewellery all over India"
    val signInIdentityLabel = "Email address"
    val signInEmailPlaceholder = "Enter your email"
    val signInChange = "Change"
    val signInPasswordLabel = "Password"
    val signInPasswordPlaceholder = "Enter your password"
    val signInForgot = "Forgot Password?"
    val signInRemember = "Remember me"
    val signInSubmitIdle = "Continue"
    val signInSubmitBusy = "Signing in..."
    val employeeDeactivated = "Your account has been deactivated by the store administrator."

        // MARK: - OTP · /entry_page/signup/verify-otp

    val otpHeadingLine1 = "Create a"
        /// Hard-coded on the web even when `?role=retailer`.
    val otpHeadingLine2 = "Wholesaler Account"
    val otpSentPrefix = "The 8-digit OTP has been sent to you at"
    val otpEdit = "Edit"
        /// Hard-coded on the web — the "2" never reflects the real remaining count.
    val otpWrong = "Invalid OTP! 2 attempts remaining"
    val otpIncomplete = "Please enter the complete 8-digit code."
    val otpGenericError = "Invalid OTP. Please try again or resend."
    val otpLocked = "All 5 resend attempts used. Priority locked."
    val otpValidPrefix = "OTP valid for "
    val otpResendIdle = "Resend OTP"
        /// Note the single-character ellipsis (U+2026), unlike the other busy labels.
    val otpResendBusy = "Sending…"
    val otpInfoNote = "Only wholesalers accounts will be verified. Retailers will require an invite to sign in."
    val otpSubmitIdle = "Continue"
    val otpSubmitBusy = "Verifying..."

    val otpValiditySeconds = 60
    val otpResendCooldownSeconds = 30
    val otpMaxResends = 5
    val otpDigitCount = 8

        // MARK: - Set password · /entry_page/signup/set-password

    val setPasswordHeading = "Set Password"
    val setPasswordSubline = "Password must contain at least 8 characters and include a capital letter, a small letter, a number and a special character"
    val setPasswordLabel = "Password"
    val setPasswordConfirmLabel = "Confirm Password"
        /// 10 × U+2022
    val setPasswordPlaceholder = "••••••••••"
    val setPasswordMismatch = "Passwords do not match"
    val setPasswordFallbackError = "Failed to set password. Please try again."
    val setPasswordInfoNote = "Only wholesalers accounts will be verified. Retailers will require an invite to sign in."
    val setPasswordSubmitIdle = "Continue"
    val setPasswordSubmitBusy = "Verifying..."

        // MARK: - Forgot password · /forgot-password

    val forgotHeading = "Reset your password"
    val forgotSubheading = "Enter your email to receive a password reset link."
    val forgotLabel = "Email address"
    val forgotPlaceholder = "Enter your email"
    val forgotSubmitIdle = "Send reset link"
    val forgotSubmitBusy = "Sending reset link..."
    val forgotBackToSignIn = "Back to sign in"
    val forgotSuccessTitle = "Check your email"
    val forgotSuccessBody = "We've sent a password reset link to your email address. Please check your inbox."
    val forgotReturn = "Return to sign in"
    val forgotEmailRequired = "Email is required"
    val forgotServerError = "Server error while verifying email."
    val forgotNoAccount = "No account found with this email address."

        // MARK: - Update password · /update-password

    val updateHeading = "Set new password"
    val updateSubheading = "Please enter your new password below."
    val updateNewLabel = "New Password"
    val updateNewPlaceholder = "Enter new password"
    val updateConfirmLabel = "Confirm Password"
    val updateConfirmPlaceholder = "Confirm new password"
        /// Trailing period — differs from `setPasswordMismatch`, deliberately.
    val updateMismatch = "Passwords do not match."
    val updateTooShort = "Password must be at least 6 characters long."
    val updateSubmitIdle = "Update password"
    val updateSubmitBusy = "Updating..."
    val updateSuccessTitle = "Password updated!"
    val updateSuccessBody = "Your password has been changed successfully."
        /// The web's label says "Dashboard" but links to sign-in. Kept verbatim.
    val updateSuccessCTA = "Continue to Dashboard"

        // MARK: - Select role · /select-role

    val selectRoleHeading = "How will you use Jewels India?"
    val selectRoleSub = "Select your role so we can tailor your experience."
    val selectRoleSub2 = "This cannot be changed later."
    val selectRoleWholesalerTitle = "Wholesaler"
    val selectRoleWholesalerBody = "Upload jewellery products, manage your catalogue, and connect with retailers."
    val selectRoleRetailerTitle = "Retailer"
    val selectRoleRetailerBody = "Browse the full jewellery catalogue, discover designs, and source from wholesalers."
    val selectRoleEmpty = "Please choose a role to continue."
    val selectRoleInvalid = "Invalid role."
    val selectRoleNotAuthed = "Not authenticated."
    val selectRoleButton = "Complete Selection"
    fun selectRoleSignedInAs(email: String): String = "Signed in as $email"

        // MARK: - The three doors (not web strings: the web has no such screen)

    val doorsHeading = "Welcome"
    val doorsSignedInHeading = "One more thing"
    val doorsSubheading = "How will you use Jewels India?"
    val doorWholesalerTitle = "I'm a wholesaler"
    val doorWholesalerBody = "List your designs for retailers."
    val doorRetailerTitle = "I'm a retailer"
    val doorRetailerBody = "Source designs. Invitation code required."
    val doorStaffTitle = "I'm a store employee"
    val doorStaffBody = "Sign in with your store username."
    val doorsHaveAccount = "Already have an account?"
    val doorsSignIn = "Sign in"
    val roleAlreadySet = "This account is already set up as something else. Sign in with a different account."
    val roleNotAllowed = "Staff accounts are created by the store — ask your store owner to add you."

        // MARK: - Invitation code

    val inviteHeading = "Your invitation"
    val inviteSubheading = "Retailers join Jewels India with an invitation from a wholesaler. Enter the code they shared with you."
    val inviteFieldLabel = "Invitation code"
    val inviteFieldPlaceholder = "PJ-A8K3X2"
    val inviteChecking = "Checking…"
    val inviteValid = "This invitation is good."
    val inviteContinue = "Continue"
    val inviteNoCode = "Don't have a code? Ask a wholesaler you work with."
    val inviteCodeEmpty = "Enter your invitation code."
    val invitationDetected = "Retailer invitation detected"
    val invitationNext = "Continue with your mobile number, email or Google. You can enter the app after Jewels India verifies your store."
    fun invitedBy(name: String): String = "Invited by $name"
    fun inviteCodeReason(reason: String?): String = when (reason) {
        "expired" -> "This invitation has expired. Ask the wholesaler for a new one."
        "used" -> "This invitation has already been used."
        "inactive" -> "This invitation isn't active any more. Ask the wholesaler for a new one."
        "no_code" -> inviteCodeEmpty
        else -> "We couldn't find that code. Check it with the wholesaler who invited you."
    }

        // MARK: - Entry, per door

    val entrySignInHeading = "Welcome back"
    val entrySignInSubheading = "Sign in to explore Jewellery all over India"
    val entryWholesalerHeading = "Create a wholesaler account"
    val entryRetailerHeading = "Create a retailer account"
    val entrySignupSubheading = "Continue with your mobile number, email or Google."
    val entryNoAccount = "We couldn't find an account for that. New to Jewels India? Go back and choose how you'll use it."
    val otpHeadingRetailer = "Retailer Account"
    val otpInfoNoteWholesaler = "Jewels India reviews every wholesaler before they can start."
    val otpInfoNoteRetailer = "Your store will be reviewed by Jewels India once you've sent your details."

        // MARK: - Staff sign-in

    val staffSubheading = "Sign in with the username and password your store gave you."
    val staffUsernameLabel = "Username"
    val staffUsernamePlaceholder = "priya.pinejewels"
    val staffForgotHint = "Forgot your password? Your store owner can reset it from their staff list."
    val staffGoogle = "Continue with Google"
    val staffWrongCredentials = "That username and password don't match. Check them with your store owner."
    val staffNotInvited = "This Google account isn't on any store's staff list yet. Ask your store owner to add it."
    val staffGoogleRequired = "Staff added by email sign in with Google."
    val staffAccountHasAnotherRole = "This Google account already belongs to a wholesaler or retailer. Use a different one for staff."

        // MARK: - Unreachable

    val unreachableTitle = "Couldn't reach Jewels India"
    val unreachableBody = "Check your connection and try again."
    val unreachableRetry = "Try again"
    val unreachableRetrying = "Trying…"

        // MARK: - Employee login · /employee-login

    val employeeHeadingLine1 = "The catalogue"
    val employeeHeadingLine2 = "is waiting."
    val employeeEmailLabel = "Email"
    val employeeEmailPlaceholder = "Enter"
    val employeePasswordLabel = "Password"
        /// 12 × U+2022
    val employeePasswordPlaceholder = "••••••••••••"
    val employeeSubmitIdle = "Get Started"
    val employeeSubmitBusy = "Signing in..."

        // MARK: - Logout confirmation (wholesaler Sidebar)

    val logoutTitle = "Confirm Logout"
    val logoutBody = "Are you sure you want to logout?"
    val logoutCancel = "Cancel"
    val logoutConfirm = "Logout"

        // MARK: - Tab labels (from the mobile bottom nav)

    object WholesalerTab {
        val home = "Home"
        val catalogue = "Catalogue"
        val chamak = "Chamak"
        val orders = "Orders"
        val chat = "Chat"
        val addRetailer = "Add Retailer"
        val inviteRetailer = "Invite Retailer"
    }

    object RetailerTab {
        val dashboard = "Dashboard"
        val catalogue = "Catalogue"
        val employees = "Employees"
        val yourTaste = "Wishlists"
        val orders = "Orders"
    }

    object EmployeeTab {
        val home = "Home"
        val catalogue = "Catalogue"
        val queries = "Queries"
        val orders = "Orders"
    }

    // MARK: - Catalogue Sharing (CONTRACT.md §5)

    object CatalogueShare {
        const val selectButton = "Select"
        const val selectCancel = "Cancel"
        fun selectCount(n: Int): String = "$n selected"
        fun shareBarButton(n: Int): String = "Share $n ${if (n == 1) "design" else "designs"}"
        const val selectLimit = "You can share up to 50 designs in one link."
        const val selectUnpublished = "Publish this design to share it."
        const val sharedLinksMenu = "Shared links"
        const val sheetTitle = "Share designs"
        const val sheetIntro = "No sign-in needed. Each browser gets one viewing session, lasting up to 30 minutes. Opening the link uses a slot."
        const val titleField = "Title (optional)"
        const val titlePlaceholder = "Designs for you"
        fun viewersLabel(n: Int): String = "Maximum viewers: $n"
        const val validityLabel = "Link validity"
        const val validity1h = "1 hour"
        const val validity24h = "24 hours"
        const val validity7d = "7 days"
        const val validityCustom = "Custom duration"
        const val customField = "Minutes (5–10,080)"
        const val privacyNote = "Shares only the designs you selected, with their photos and names. Your other designs, prices and contact details stay private."
        const val createButton = "Create link"
        const val createBusy = "Please wait…"
        const val readyHeader = "Your link is ready"
        const val readyShare = "Share link"
        const val readyCopy = "Copy link"
        const val readyCopied = "Link copied."
        fun readyExpires(dateText: String): String = "Expires $dateText"
        const val readyWarning = "Copy this link now. It cannot be retrieved later."
        const val listHeader = "Recent links"
        const val listLoading = "Loading links…"
        const val listEmpty = "No links created yet."
        const val listOffline = "Recent links will refresh when your connection is available."
        const val listRefresh = "Refresh links"
        fun listRow(itemCount: Int, viewsUsed: Int, maxViewers: Int): String =
            "$itemCount designs · $viewsUsed / $maxViewers slots used"
        const val statusActive = "Active"
        const val statusRevoked = "Revoked"
        const val statusExpired = "Expired"
        const val statusFull = "All slots used"
        const val revokeButton = "Revoke link"
        const val revokeDone = "Link revoked. Further access is blocked."
        fun shareText(title: String?, url: String): String =
            if (!title.isNullOrBlank()) "$title — $url" else url
    }
}

