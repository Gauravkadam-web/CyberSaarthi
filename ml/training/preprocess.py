"""
CyberSaarthi — Preprocessing & Stratified Splitting Pipeline
Phase 3 / Task 3.1
============================================================
Processes raw audited cybercrime dataset, integrates curated minority
exemplars to achieve full 8-class taxonomy coverage, and partitions
into stratified Train (70%), Validation (15%), and Test (15%) splits
with random_seed=42.

Usage:
    python ml/training/preprocess.py
"""

import os
import re
import hashlib
from pathlib import Path
import pandas as pd
import numpy as np
from sklearn.model_selection import train_test_split

# ---------------------------------------------------------------------------
# Configuration
# ---------------------------------------------------------------------------
RAW_CSV_PATH = Path("ml/data/raw/cybercrime_raw.csv")
PII_FLAG_PATH = Path("ml/data/audit_output/pii_flagged_rows.csv")
PROCESSED_DIR = Path("ml/data/processed")
RANDOM_SEED = 42

TAXONOMY_V1_CODES = [
    "FINANCIAL_UPI_FRAUD",
    "PHISHING_SOCIAL_ENGINEERING",
    "IDENTITY_THEFT",
    "SOCIAL_ACCOUNT_COMPROMISE",
    "CYBERSTALKING_HARASSMENT",
    "JOB_INVESTMENT_SCAM",
    "MALWARE_RANSOMWARE",
    "OTHER_UNCLASSIFIED",
]

# ---------------------------------------------------------------------------
# Curated Synthetic Exemplars for Minority Categories (C-5 Fallback)
# Strictly aligned with docs/taxonomy-v1.0.md specifications
# ---------------------------------------------------------------------------
SYNTHETIC_EXEMPLARS = {
    "MALWARE_RANSOMWARE": [
        "My laptop screen turned completely black and all my documents and project files now have a strange .locked extension. A notepad file demands 0.05 Bitcoin to decrypt.",
        "I downloaded an invoice PDF attachment from an email and my computer froze. All my family photos and documents are encrypted with ransomware and asking for money.",
        "A popup told me to update my Flash player to view a video. After installing the exe, all my files became inaccessible and a countdown timer appeared demanding payment.",
        "Someone called posing as tech support and instructed me to install AnyDesk. Once installed, they took remote control of my laptop and locked my system with a password.",
        "My phone suddenly started lagging heavily and battery draining in 1 hour. Unknown app called SystemUpdate is sending SMS and reading my screen continuously.",
        "A malicious APK file was shared on a WhatsApp group for viewing cricket match. After installing, it gained accessibility permissions and blocked uninstallation.",
        "Ransomware attacked our small business computer. All accounting tally files and customer invoices are encrypted with .crypt extension demanding ransom.",
        "My smartphone got infected with spyware. Strange ads are constantly popping up even on home screen and microphone icon stays active in status bar.",
        "Downloaded a cracked software for photo editing and my anti-virus alert warned about a Trojan and keylogger recording my keystrokes.",
        "A locker malware locked my mobile screen with a fake police notice claiming I visited illegal websites and demanding an immediate fine to unlock the device.",
        "My office PC was infected via a USB drive. A ransomware note appeared on desktop background asking to email the attackers for file restoration.",
        "Fake WhatsApp Gold APK downloaded from third-party website has taken control of my device and is sending spam links to all my contacts automatically.",
        "All my Excel spreadsheets and PDF bills turned into .cryptolocker format. The extortionists left a readme.txt file with instructions to transfer cryptocurrency.",
        "An untrusted browser extension hijacked my browser homepage and started injecting malicious redirect links and popunder ads into every webpage.",
        "Installed an app from outside Google Play Store that claimed to give free mobile recharge. Now device is locked and demanding administrative PIN.",
        "A banking trojan overlay appeared on top of my official bank app asking me to re-enter my mPIN and card CVV details.",
        "My system files were replaced by a malicious worm that replicated across my home Wi-Fi network and corrupted shared folders.",
        "Malicious script executed from an infected website download, locking my desktop and displaying continuous ransom threats.",
        "Ransomware infected my thesis files right before my semester submission. The files cannot be opened and demand payment to a crypto wallet address.",
        "Remote Access Trojan installed stealthily on my laptop without my knowledge, turning on webcam and capturing screenshots of my screen.",
        "Fake antimalware tool claimed my computer has 500 viruses and locked all system settings unless I buy their premium license for Rs 5000.",
        "My Android phone is locked with a persistent overlay claiming to be from the cyber cell demanding Rs 2000 fine via gift card.",
        "Downloaded a cracked game torrent and now all my hard drive partitions are encrypted with .dark extensions demanding ransom.",
        "A malicious keylogger program was detected by Windows Defender running in background processes logging all my passwords.",
        "My phone was hijacked by an aggressive adware that forces full screen video advertisements every 30 seconds and installs apps without consent.",
        "Ransomware hit my desktop computer and encrypted all my client contracts with an RSA 2048 key. The attacker left a Telegram handle to negotiate.",
        "Fake courier tracking application installed as an APK that intercepts all incoming SMS messages including OTPs and forwards them secretly.",
        "A malicious payload infected my browser cookies and session tokens, allowing the attacker to hijack my active sessions without password.",
        "Laptop was locked with a red screen warning that my data will be permanently deleted in 24 hours if payment is not received in Monero.",
        "Downloaded a font pack from an unknown website and my PC began encrypting all files in C drive with .locked format.",
        "Spyware installed on my phone through a malicious link that records ambient audio through microphone and uploads to an external server.",
        "An application posing as a PDF reader requested root access and installed a rootkit that disabled all security software.",
        "Ransomware encrypted my entire photo collection and my family video archives, demanding an extortion fee within 48 hours.",
        "A malicious macro in a Microsoft Word email attachment downloaded a payload that encrypted all files on my network drive.",
        "Fake electricity payment app downloaded via SMS link locked my phone screen completely and started vibrating continuously.",
        "A trojan infected my system and added my laptop to a botnet that executes DDoS attacks without my permission.",
        "Computer infected with screenlocker malware that blocks keyboard and mouse access and demands an online voucher to unlock.",
        "My smartphone storage was wiped by a destructive wiper malware disguised as an antivirus scanner.",
        "Ransomware encrypted all my CAD engineering drawings and blueprints, leaving an extortion note on my desktop.",
        "Malicious APK disguised as a government welfare scheme application hijacked SMS permissions and drained my mobile balance.",
        "A covert keylogger captured my banking credentials and transmitted them to a remote command and control server.",
        "Ransomware encrypted our hospital pharmacy management database with an extension .payusnow and left instructions to buy Bitcoin.",
        "An adware app disguised as a wallpaper gallery keeps installing malicious utility tools and drains device battery rapidly.",
        "Trojan horse disguised as a video codec update compromised my Windows administrator privileges and created backdoor accounts.",
        "Ransomware locked all our college project source codes and git repository folders, demanding a ransom payment.",
        "Malicious remote access tool installed via phishing email enabled an intruder to browse through my personal photos and tax filings.",
        "My device was infected by an SMS stealer malware that automatically deletes incoming transaction SMS so I do not notice deductions.",
        "Screenlocker malware appeared pretending to be Windows Security center with an emergency alarm sound and phone number to call.",
        "A malicious software altered my DNS settings and redirected all secure banking URLs to cloned phishing servers.",
        "Ransomware encrypted my personal accounting files and demands 0.1 ETH to an anonymous wallet within three days.",
        "Infected with a crypto-mining malware that uses 100% of my CPU and GPU causing extreme overheating and system crashes.",
        "A malicious utility app secretly subscribed my phone number to premium rate SMS services causing huge mobile bills.",
        "Ransomware corrupted my graphic design portfolio files and asks for USD 300 via gift cards to release decryption key.",
        "A trojan downloaded through a pirate movie streaming site installed a stealth VNC server granting remote screen access.",
        "Malicious mobile application installed silently from a browser redirect, taking administrative device manager control.",
        "Ransomware encrypted our educational institute exam question paper repository, displaying an urgent extortion message.",
        "A backdoor trojan allowed unauthorized remote file transfer of my confidential office documents to an external IP address.",
        "Downloaded a utility tool that came bundled with a silent ransomware payload which triggered and locked all documents at midnight.",
        "My Android tablet is completely unresponsive and displaying a locked banner demanding immediate payment to an online wallet.",
        "A worm spread through our shared home Wi-Fi and infected three laptops, corrupting document files and demanding ransom."
    ],
    "IDENTITY_THEFT": [
        "When I applied for a home loan, the bank rejected my application stating my CIBIL score dropped due to an unpaid loan of Rs 3 lakhs taken in my name without my consent.",
        "Someone used a forged copy of my PAN card and Aadhaar card to obtain five mobile SIM cards that are now being used in criminal activities.",
        "I received a recovery notice from a fintech loan app for a personal loan of Rs 50,000 which I never applied for or received.",
        "An unknown person opened a fraudulent current account in an axis bank branch using my stolen PAN details and forged electricity bill.",
        "My identity was misused by an unknown person to register a fake GST number and conduct fraudulent business transactions worth lakhs.",
        "Discovered that an imposter has rented an apartment using forged photocopies of my passport and Aadhaar card.",
        "Received a tax notice from the Income Tax department for transactions worth 50 lakhs done using my PAN card by someone else.",
        "Someone created a duplicate voter ID card in my name and used it to purchase a vehicle on loan without my authorization.",
        "My Aadhaar number was linked to an unknown Airtel payment bank wallet where fraudulent UPI transactions were being received.",
        "An unknown individual impersonated me at a telecom store with forged ID documents and issued a duplicate SIM in my name.",
        "Found out through credit bureau report that three credit cards were issued in my name to an address in another state where I never lived.",
        "A fraudulent NBFC instant loan of Rs 80,000 was disbursed to an unknown bank account using my stolen KYC documents.",
        "A company was registered under the Ministry of Corporate Affairs using my forged digital signature and PAN without my consent.",
        "Someone used my driver's license photo and details to create a fake delivery partner profile on an e-commerce platform.",
        "Recovery agents are calling my family for an unpaid bike loan taken by an imposter using my stolen identity proofs.",
        "An unknown person obtained a passport using forged documents matching my personal name and father's name.",
        "My stolen Aadhaar photocopy was misused to buy multiple pre-activated SIM cards that were later traced by police in an extortion case.",
        "Someone opened an online demat trading account in my name and traded penny stocks using my PAN information.",
        "Received a legal notice for cheque bounce from a bank where I never opened any account; someone forged my signature and identity.",
        "Discovered that a post-paid mobile connection with huge pending bills was activated using my voter ID card without my knowledge.",
        "My personal details and photo from LinkedIn were used to forge an employee identity card to enter corporate premises illegally.",
        "An imposter used my PAN card to claim fraudulent tax refunds from the Income Tax department into their own bank account.",
        "Received credit card statement for an account I never applied for, showing transactions in foreign currency.",
        "Someone registered a commercial vehicle under my name using forged documents and it was involved in an accident.",
        "Discovered that an unknown person has been drawing government subsidy benefits using my duplicate Aadhaar credentials.",
        "A fake microfinance loan was sanctioned in my name in a rural branch where I have never visited in my life.",
        "My identity was stolen to create a fraudulent verified merchant profile on an online payment gateway.",
        "Someone used my stolen PAN and salary slip photocopies to lease office equipment worth several lakhs.",
        "A credit report check showed a defaulted personal loan of Rs 1,20,000 taken through an online instant credit app in my name.",
        "An imposter obtained an educational loan from a cooperative bank using forged marksheets and my national identity card.",
        "Received an SMS from UIDAI that my Aadhaar was authenticated for a SIM purchase in a state where I have never been.",
        "Someone used my name and forged signature on property registration documents as a dummy guarantor for a loan.",
        "A fraudster used my personal KYC details to create a fake cryptocurrency exchange account to launder illicit funds.",
        "Unknown person took an instant mobile phone EMI loan at a local retail store using a morphed copy of my Aadhaar card.",
        "Received collection notices for high-value purchases on a BNPL pay-later service that was activated using my PAN card.",
        "An imposter used my medical insurance card and policy details to claim fraudulent hospital reimbursement.",
        "A fake bank account was opened using my forged identity proofs and used to receive money from cyber fraud victims.",
        "Someone applied for a credit card in my name by altering the address on a forged utility bill copy.",
        "Discovered that my PAN was registered as a director in a shell company that is under tax investigation.",
        "A stranger used my identity documents to register a fraudulent domain name and host abusive web content.",
        "Received calls from a legal firm regarding a loan default on an online app where my Aadhaar card was uploaded fraudulently.",
        "An unknown person obtained a duplicate driving license using my identity records from an RTO in another district.",
        "Someone linked my PAN card to their e-wallet and transacted large sums of undeclared money.",
        "My identity was used to open an unauthorized trading account that engaged in illegal circular trading.",
        "A criminal suspect arrested in another state possessed a forged Aadhaar card carrying my exact name and UID number.",
        "Discovered an active consumer durable loan for an expensive television set taken in my name with forged signature.",
        "An unauthorized loan of Rs 2,50,000 is showing on my Experian report from an online lending platform I never used.",
        "Someone registered a forged electricity meter connection in my name on a vacant commercial plot.",
        "Fraudulent SIM card issued using my KYC documents was used to make threatening extortion calls to a businessperson.",
        "My identity was stolen and used to apply for government welfare rations in an unauthorized district.",
        "Received court summons for non-payment of an automobile loan taken with forged PAN and bank statement.",
        "An imposter impersonated my identity to take employment at a private firm using forged credential certificates.",
        "Found out that an unknown person has taken credit card add-on cards using my primary credit profile without authorization.",
        "A forged copy of my passport was submitted to a hotel where suspicious criminal activities occurred.",
        "Someone used my stolen identity proofs to register multiple fraudulent merchant QR codes for money laundering.",
        "Discovered that a personal loan of Rs 75,000 was disbursed to an online wallet using my forged PAN and selfie verification bypass.",
        "My Aadhaar card details were used fraudulently to activate international roaming on unauthorized SIM cards.",
        "Someone forged my identity to open a postal savings account that was used as a mule account in lottery fraud.",
        "Received notice from CIBIL stating that seven loan inquiries were made in two days using my PAN details.",
        "An unknown person registered a fake enterprise on the Udyam MSME portal using my personal identity numbers."
    ],
    "SOCIAL_ACCOUNT_COMPROMISE": [
        "My Instagram account was hacked yesterday. The attacker changed my registered email and two-factor authentication, and is now sending direct messages to my followers asking them to send money via UPI urgently.",
        "I was locked out of my Facebook account. The hacker changed my password and phone number, and is posting inappropriate cryptocurrency scam advertisements on my profile.",
        "Someone hijacked my WhatsApp account by tricking me into sharing a 6-digit verification code. They are now messaging my family contacts requesting emergency funds.",
        "My Gmail account password was changed and recovery phone was updated to an unknown number. The hacker accessed my Google Drive and personal photos.",
        "A duplicate cloned profile was created on Instagram with my exact pictures and name, sending follow requests to my friends and asking for private photos.",
        "My Twitter X account was compromised. The attacker changed the handle name to a crypto token and is broadcasting spam phishing links to my followers.",
        "Lost access to my LinkedIn account. The attacker replaced my profile photo, deleted my connections, and is sending fake job offer messages to my network.",
        "Someone gained access to my Snapchat account and is threatening to leak my private saved stories unless I pay ransom on UPI.",
        "My YouTube channel with 50,000 subscribers was hijacked through session cookie theft. The hacker started a fake live stream promoting a Bitcoin giveaway.",
        "An unknown person hacked into my Telegram account and terminated my active sessions, and is now impersonating me in business groups.",
        "My Facebook business page was stolen. The hacker removed me as an admin and started running thousands of rupees worth of unauthorized ads on my linked card.",
        "I received a fake copyright infringement message on Instagram with a link. When I logged in, my account credentials were stolen and 2FA was enabled by the scammer.",
        "Someone bypassed my email two-factor authentication and forwarded all incoming emails to an external suspicious outlook account.",
        "My Instagram direct messages were accessed by an unauthorized person who is sending abusive messages to my colleagues from my account.",
        "A fraudster created a fake WhatsApp profile using my photo and is messaging my office team pretending to be me asking for urgent Google Play gift cards.",
        "My Reddit account was hijacked and the password was changed. The attacker deleted all my posts and posted malicious malware links.",
        "Someone accessed my Apple ID and locked my iPhone remotely through Find My, displaying an extortion message on screen.",
        "My Pinterest account was hacked and connected to an external spam marketing bot network.",
        "I was locked out of my Discord account. The hacker accessed my server and banned all members while sending malicious phishing links in all channels.",
        "My Instagram profile photo and bio were replaced with adult content and the attacker is demanding Rs 10,000 to return the account.",
        "Received a security alert that my Gmail was logged in from an unknown device in another country, and recovery email was immediately altered.",
        "An attacker used a session hijacking attack to access my Facebook account without triggering OTP and locked me out.",
        "Someone created an identical duplicate Facebook account with my photos and is sending friend requests asking people to donate to a fake charity.",
        "My WhatsApp web was compromised because someone scanned the QR code from my unlocked laptop and is reading all my private chats.",
        "Lost access to my TikTok account. The registered phone number was switched and all my published videos were deleted.",
        "Hacker took over my primary email account and initiated password resets for all my linked banking and social media profiles.",
        "Someone compromised my Spotify family account and added unauthorized users while changing my account credentials.",
        "My Instagram account was stolen by an influencer badge phishing scam. I clicked a link claiming to offer a blue verification tick.",
        "A hacker took control of my Twitter account and started posting defamatory remarks tagging my employer.",
        "Someone gained unauthorized access to my Google Photos account through compromised password and downloaded private family pictures.",
        "My WhatsApp account was transferred to an unknown device at midnight and an OTP was bypassed through SIM forwarding.",
        "The admin credentials for our college student union Facebook page were stolen and the page was converted into a spam portal.",
        "My LinkedIn account was accessed by an intruder who changed my work history and sent harassing messages to recruiters.",
        "Someone hacked my Instagram and set up a dynamic authenticator app app key so I cannot recover through SMS OTP.",
        "An unauthorized person logged into my Snapchat account from another city and sent extortion threats to my contact list.",
        "My Twitch streaming channel was compromised and the attacker deleted all past stream archives and stream keys.",
        "A cloned Instagram account is messaging my friends claiming that I am stuck in an emergency hospital and need Rs 5000 transferred immediately.",
        "Someone hacked my email account and deleted all my sent emails and job application correspondence.",
        "My Facebook messenger was hijacked and an automated bot is sending malicious links to all my active chat conversations.",
        "An attacker took over my Telegram channel with 10,000 members and removed all administrator privileges from my account.",
        "Received an alert that my Microsoft Outlook account password was changed from an unrecognized IP address.",
        "My Instagram account was disabled after a hacker gained access and deliberately posted content violating community guidelines.",
        "Someone compromised my WhatsApp account and added all my contacts to shady investment schemes and cryptocurrency spam groups.",
        "An unknown user accessed my Cloud storage account and changed the recovery questions and security keys.",
        "My Twitter account was compromised and is currently suspended due to automated spam posting by the attacker.",
        "Someone cloned my WhatsApp business profile and is taking fake product orders and money from my regular customers.",
        "Lost access to my primary Yahoo mail account after clicking an email verification link from a deceptive security alert.",
        "A hacker accessed my Instagram account and deleted all my personal posts and highlights, demanding money to stop deleting.",
        "My Facebook account was locked after an attacker repeatedly attempted failed logins from multiple automated bot servers.",
        "Someone compromised my Gmail account and accessed my saved Chrome passwords and auto-fill payment cards.",
        "An attacker took over my Quora account and began posting misleading financial advice using my professional credentials.",
        "My Instagram bio was modified to display an adult webcam link and the attacker blocked my family members so they would not see.",
        "Someone accessed my Discord moderator account and compromised our entire community server with destructive bot scripts.",
        "My personal email was hijacked and used to send deceptive phishing emails to all my corporate email contacts.",
        "A cloned Facebook profile was created using pictures of my minor child and is being used to solicit money from relatives.",
        "Someone gained unauthorized access to my WhatsApp account using a voice call OTP voicemail bypass vulnerability.",
        "My Instagram account was taken over after I participated in an online voting contest that asked for social media login credentials.",
        "An unauthorized device logged into my Apple iCloud account and triggered remote wipe on my iPad.",
        "The hacker changed my LinkedIn primary email to a temporary mail address and enabled hardware security key 2FA.",
        "My social media account was compromised through a credential stuffing attack using a password leaked in an old data breach."
    ],
    "JOB_INVESTMENT_SCAM": [
        "I was contacted on WhatsApp for a part-time job involving rating hotels and liking videos. Initially, they credited Rs. 500 to my account, but then asked me to deposit Rs. 50,000 for premium tasks. After I transferred the money, they blocked me.",
        "Received a job offer letter on email claiming to be from an international airline for airport ground staff. They asked me to deposit Rs 15,000 for uniform and medical verification fees.",
        "Joined a Telegram trading group promising 200% profit in 24 hours on a cryptocurrency platform. I deposited Rs 1,00,000 and when I tried to withdraw, they demanded a 30% tax deposit first.",
        "A work-from-home data entry job requested a refundable security deposit of Rs 8,500. After completing 500 pages, they rejected my work citing fake errors and refused the refund.",
        "Invested Rs 2,50,000 in an online forex trading platform recommended on Instagram. The website showed high profits but my withdrawal requests were permanently frozen.",
        "Got a message on Telegram offering a daily income of Rs 3,000 for completing simple merchant review tasks on Amazon. I lost Rs 45,000 in prepaid task recharge fees.",
        "A recruiter claiming to represent a Canadian consultancy promised a guaranteed work visa and job in Toronto, taking Rs 1,80,000 for fake visa processing.",
        "Joined an online investment scheme offering 15% guaranteed monthly return through algorithmic stock trading. After six months the promoters disappeared and the app shut down.",
        "Received an SMS for an interview for a government railway job. Paid Rs 25,000 for fake exam admit card and interview training at a fake recruitment center.",
        "An online task scam lured me into depositing small amounts on a fake portal. After depositing Rs 75,000 for VIP tasks, the manager deleted the Telegram group.",
        "A crypto investment platform app downloaded via a Telegram link promised daily compounding interest. After transferring 0.5 Bitcoin, my account was locked.",
        "Offered a part-time job as a movie reviewer. Earned Rs 800 on day one, then deposited Rs 60,000 in successive task levels before they stopped responding.",
        "Paid Rs 12,000 as registration and document verification charge for an overseas nursing job offer that turned out to be completely fictitious.",
        "An investment advisory firm on WhatsApp promised multi-bagger stock tips and asked me to deposit funds into a pooled trading account, taking Rs 1,50,000.",
        "A fraudulent work-from-home packaging job took Rs 5,000 for sending raw material which was never delivered, and all contact numbers are switched off.",
        "Invested Rs 80,000 in a gold trading robot scheme promoted by an Instagram influencer. The website closed down within a week of investment.",
        "Received a job offer for a data analyst position from a fake HR email. They sent a fraudulent check for office equipment and asked me to refund the difference.",
        "A fake app called CloudMining promised high daily USDT returns for renting digital mining servers. Deposited Rs 1,20,000 which cannot be withdrawn.",
        "Offered a lucrative typing job paying Rs 50 per page. Paid Rs 3,500 for portal access software which was completely non-functional.",
        "A Telegram group admin claimed to possess insider trading tips and promised guaranteed doubling of funds in 3 days, defrauding me of Rs 50,000.",
        "Applied for a bank clerk vacancy on a cloned job portal. Paid Rs 6,500 for application form and interview letter, but the bank confirmed it was a scam.",
        "Joined an online lottery and spin-the-wheel task program. Deposited Rs 35,000 to unlock my supposed winnings of Rs 5 lakhs, which were never paid.",
        "An online multi-level marketing pyramid scheme promised passive income for recruiting members and investing Rs 25,000 in digital health vouchers.",
        "Paid Rs 20,000 to an overseas recruitment agency for a hospitality job in Dubai. The agency vanished after collecting passports and money.",
        "A fraudulent investment platform showed my balance growing to Rs 10 lakhs from an initial deposit of Rs 1 lakh, but demanded Rs 2 lakhs in fees to withdraw.",
        "Offered a part-time job subscribing to YouTube channels and taking screenshots. After earning initial commission, I was duped into investing Rs 85,000.",
        "Fake tech company interview conducted over Skype. They sent a fake appointment letter and asked for Rs 30,000 for laptop security deposit.",
        "Invested in a peer-to-peer lending scheme promising 24% annual returns. The company stopped paying interest and the directors fled.",
        "A work-from-home SMS sending job required buying an expensive SMS gateway package for Rs 15,000 that never generated any income.",
        "Telegram crypto bot promised automated arbitrage profits. Transferred Rs 40,000 to the wallet address and the bot blocked my account.",
        "An agency offered a fake merchant navy job and extracted Rs 2,00,000 in the name of CDC certificate and maritime medical clearance.",
        "Invested Rs 1,50,000 in a fraudulent mutual fund clone portal that imitated a reputed asset management company.",
        "A fake freelance writing agency charged a registration fee of Rs 4,500 and disappeared without assigning any projects.",
        "Lured into a fake binary options trading platform with manipulated price charts where every trade resulted in artificial liquidation of funds.",
        "Paid Rs 18,000 for an online training certification that falsely guaranteed a placement with a multinational technology corporation.",
        "An investment scheme on Telegram promised daily dividends from agricultural export projects. Lost Rs 65,000 when the website vanished.",
        "Offered a job as a customer service executive from home. Sent Rs 10,000 for biometric attendance machine which never arrived.",
        "A high-yield investment program (HYIP) website promised 5% daily returns on Bitcoin. Deposited funds and the site domain expired.",
        "Paid Rs 50,000 to a consulting firm for an IT job in Germany. All documents and visa interview letters provided were forged.",
        "A part-time product tester scam on WhatsApp took Rs 40,000 in security fees for high-value electronics that were never dispatched.",
        "Invested Rs 2,00,000 in a fake real estate crowdfunding token that turned out to be an exit scam rug pull.",
        "A fraudulent portal claiming to be an official government recruitment board charged Rs 1,500 application fee from thousands of applicants.",
        "Online survey task platform promised Rs 500 per survey. After accumulating Rs 10,000 in credits, the portal refused payouts without a VIP upgrade fee.",
        "An investment scheme guaranteed doubling of money through renewable energy carbon credits, taking Rs 90,000 from my savings.",
        "Offered a part-time social media manager position. Deposited Rs 22,000 as refundable software access fee which was stolen.",
        "Fake overseas modeling contract took Rs 75,000 for portfolio photoshoot and grooming charges before disconnecting all communications.",
        "Invested in an algorithmic crypto trading pool managed by an anonymous administrator who emptied the liquidity pool.",
        "A job agency promised immediate placement in a leading automobile manufacturing plant for an upfront commission of Rs 35,000.",
        "Online captcha entry work demanded Rs 2,000 registration fee and set impossible accuracy benchmarks to forfeit earned earnings.",
        "An unregulated private chit fund app lured investors with attractive prize schemes and abruptly terminated operations with our savings.",
        "Paid Rs 14,000 for medical representative job kit and product samples that were never shipped.",
        "Invested Rs 1,10,000 in a fake foreign currency exchange trading software that manipulated demo balances to trick users into depositing real cash.",
        "A fraudulent work-from-home proofreading job charged Rs 5,500 for sample material and blocked me upon submission.",
        "Promised a lucrative part-time job promoting luxury brands on Instagram. Lost Rs 48,000 in advance task deposits.",
        "An investment group on WhatsApp promised pre-IPO shares of a unicorn startup at discounted prices, taking Rs 3,00,000.",
        "Paid Rs 25,000 for a pilot training cadet program selection letter which was identified as forged by the aviation academy.",
        "A fake digital marketing agency hired me for link-building work and disappeared without paying two months of accumulated salary.",
        "Invested in a decentralized finance (DeFi) staking platform that had a hidden backdoor contract draining user deposits.",
        "Offered a part-time hotel reservation booking agent job. Transferred Rs 55,000 to unlock booking commissions that were never released.",
        "A recruitment scam promised a permanent government teaching position for an upfront processing bribe of Rs 1,50,000."
    ],
    "OTHER_UNCLASSIFIED": [
        "Something suspicious happened with my router settings and internet speed is very slow. I am not sure if someone is tapping my connection.",
        "I noticed strange network traffic on my smart television and smart home speaker. How can I verify if my home devices are compromised?",
        "My printer started printing random gibberish pages automatically in the middle of the night without any print command sent.",
        "I received a confusing email in a foreign language with an invoice number that does not match any of my orders. Not sure what it means.",
        "Someone left an unmarked USB flash drive on my office desk. I have not plugged it in and need guidance on safe disposal.",
        "My Bluetooth keeps turning on automatically on my smartphone even after turning it off. Is this a security issue?",
        "Received multiple verification code SMS messages from services I do not use, but no unauthorized logins have occurred on my accounts.",
        "My home Wi-Fi network name was changed automatically to an unknown name. Could a neighbor have accessed my router admin portal?",
        "A website I regularly visit showed a security certificate warning saying the connection is not private. Should I be concerned?",
        "I found a tracking device airtag in my backpack that does not belong to me or my family. How do I report this?",
        "My smart watch display showed a pairing request from an unknown phone number while I was traveling on the metro.",
        "Received a physical letter in mail with a strange QR code claiming to be a survey for digital lifestyle.",
        "The battery of my digital security camera drains within hours and camera keeps tilting automatically towards the wall.",
        "My public IP address appears on an online blacklist according to an email from my internet service provider.",
        "Found a suspicious hardware device plugged into the back of my desktop computer tower at my workplace.",
        "Received an anonymous phone call with distorted electronic noise and breathing sounds that disconnected after 10 seconds.",
        "My browser search engine keeps changing to a suspicious search portal despite resetting browser settings multiple times.",
        "An unknown Bluetooth device named AudioReceiver is continuously attempting to pair with my mobile phone in my apartment.",
        "I noticed an unrecognized user profile created on my Windows login screen named DefaultUser101.",
        "My smart home light bulbs are turning on and off at odd hours without any schedule set on my app.",
        "Received a suspicious WhatsApp audio message from an unknown international country code that I did not download.",
        "My mobile network carrier signal drops to emergency calls only frequently in my neighborhood.",
        "A strange executable file named test_script.bat appeared on my desktop screen after a Windows update.",
        "I suspect that someone is monitoring my internet browsing history through a shared public Wi-Fi network at my hostel.",
        "My laptop microphone indicator LED light flickers briefly when turning on the machine before any application opens.",
        "Received an email from my own email address stating that my computer is monitored, but no ransom amount is specified.",
        "The time and date settings on my smartphone keep resetting to an incorrect year automatically.",
        "Found a strange configuration profile installed in my phone settings under VPN and Device Management.",
        "My smart TV microphone remote is picking up ambient conversations and displaying text suggestions on screen.",
        "An unrecognized device with an unknown MAC address is connected to my residential broadband router.",
        "Received an unsolicited parcel from an e-commerce site with cheap sunglasses that I never ordered or paid for.",
        "My car infotainment system rebooted repeatedly while connecting my phone via USB cable.",
        "A website login page has two password fields instead of one and looks slightly misaligned compared to normal.",
        "My mobile data consumption jumped by 10 GB in one day while the phone was connected to home Wi-Fi overnight.",
        "Received an automated phone call asking me to press 1 to confirm my voter registration details.",
        "My computer cooling fan runs at maximum speed even when no software is open on task manager.",
        "A strange icon appeared in my notification tray that disappears whenever I move the mouse cursor over it.",
        "Someone dropped a memory card with hand-written labels outside my apartment door.",
        "My browser downloads folder contains several temporary files with .tmp extensions created without my knowledge.",
        "An unknown application named ServiceHost requested firewall network access on my home computer.",
        "My online cloud backup service uploaded 500 files during the night that I do not recognize from my hard drive.",
        "Received an SMS notification that an unknown device was added to my home media sharing group.",
        "My GPS navigation app shows my vehicle location in another city when I am parked in my garage.",
        "A suspicious popup appeared while browsing a news site asking to install a security certificate for fast browsing.",
        "My office email inbox received an empty message with a calendar invite from an unrecognizable domain.",
        "The webcam light on my external monitor remains dimly lit even when the monitor is on standby mode.",
        "Received a text message containing only random alphanumeric characters and a timestamp with no links.",
        "My smartphone screen sensitivity is acting erratically and registering phantom touches near the keyboard area.",
        "Found an unauthorized shared network printer visible on my home Wi-Fi network.",
        "An unfamiliar login prompt appeared on my screen asking for network credentials while using a calculator app."
    ]
}

# ---------------------------------------------------------------------------
# Text Cleaning
# ---------------------------------------------------------------------------
def clean_text(text: str) -> str:
    """Normalizes incident text while preserving incident terminology."""
    text = str(text).strip()
    # Replace URLs
    text = re.sub(r"https?://\S+|www\.\S+", " url_link ", text)
    # Replace emails
    text = re.sub(r"\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Z|a-z]{2,}\b", " email_address ", text)
    # Replace phone numbers
    text = re.sub(r"\b[6-9]\d{9}\b", " phone_number ", text)
    # Normalize excessive whitespace
    text = re.sub(r"\s+", " ", text).strip()
    return text.lower()


# ---------------------------------------------------------------------------
# Main Preprocessing Routine
# ---------------------------------------------------------------------------
def run_preprocessing() -> None:
    PROCESSED_DIR.mkdir(parents=True, exist_ok=True)
    print("=" * 70)
    print("  CyberSaarthi — Data Preprocessing & Stratified Splitting")
    print("=" * 70)

    # 1. Load raw dataset
    if not RAW_CSV_PATH.exists():
        raise FileNotFoundError(f"Raw CSV not found at {RAW_CSV_PATH}")

    df_raw = pd.read_csv(RAW_CSV_PATH)
    print(f"Raw CSV loaded: {RAW_CSV_PATH} ({len(df_raw):,} records)")

    # 2. Exclude PII rows if audit output exists
    if PII_FLAG_PATH.exists():
        pii_df = pd.read_csv(PII_FLAG_PATH)
        df_clean = df_raw[~df_raw.index.isin(pii_df["row_index"])].copy()
        print(f"Dropped {len(pii_df)} PII-flagged rows -> {len(df_clean):,} remaining")
    else:
        df_clean = df_raw.copy()

    # 3. Filter out 'legit' benign class
    df_incidents = df_clean[df_clean["category_name"] != "legit"].copy()
    print(f"Excluded 'legit' benign rows -> {len(df_incidents):,} cyber incident records")

    # 4. Map raw categories to Taxonomy v1.0 codes
    category_mapping = {
        "financial_fraud": "FINANCIAL_UPI_FRAUD",
        "government_impersonation": "PHISHING_SOCIAL_ENGINEERING",
        "bank_phishing": "PHISHING_SOCIAL_ENGINEERING",
        "romance_parcel_sextortion": "CYBERSTALKING_HARASSMENT",
    }
    df_incidents["taxonomy_category"] = df_incidents["category_name"].map(category_mapping)
    df_incidents["clean_text"] = df_incidents["text"].apply(clean_text)
    df_incidents["source"] = "raw_sentinel"

    base_records = df_incidents[["clean_text", "taxonomy_category", "source"]].copy()
    print("\nBase incident records from raw dataset:")
    print(base_records["taxonomy_category"].value_counts())

    # 5. Add curated synthetic exemplars for minority categories
    synthetic_records = []
    for cat_code, texts in SYNTHETIC_EXEMPLARS.items():
        for t in texts:
            synthetic_records.append({
                "clean_text": clean_text(t),
                "taxonomy_category": cat_code,
                "source": "curated_synthetic"
            })
    df_synth = pd.DataFrame(synthetic_records)
    print(f"\nAdded {len(df_synth)} curated synthetic exemplars across {len(SYNTHETIC_EXEMPLARS)} minority classes:")
    print(df_synth["taxonomy_category"].value_counts())

    # 6. Combine datasets
    df_full = pd.concat([base_records, df_synth], ignore_index=True)
    
    # Remove any potential duplicate texts
    df_full["text_hash"] = df_full["clean_text"].apply(lambda t: hashlib.md5(t.encode()).hexdigest())
    before_dedup = len(df_full)
    df_full = df_full.drop_duplicates(subset=["text_hash"]).drop(columns=["text_hash"]).reset_index(drop=True)
    print(f"\nFull combined dataset after deduplication: {len(df_full):,} rows (removed {before_dedup - len(df_full)} duplicates)")

    print("\nFinal Class Distribution across all 8 Taxonomy v1.0 Categories:")
    class_counts = df_full["taxonomy_category"].value_counts()
    for cat, cnt in class_counts.items():
        pct = (cnt / len(df_full)) * 100
        print(f"  {cat:<35}: {cnt:>5} ({pct:>5.1f}%)")

    # 7. Stratified Splitting: 70% Train, 15% Validation, 15% Test
    # Step 1: Split into Train (70%) and Temp (30%)
    X = df_full["clean_text"]
    y = df_full["taxonomy_category"]
    
    X_train, X_temp, y_train, y_temp = train_test_split(
        X, y,
        test_size=0.30,
        random_state=RANDOM_SEED,
        stratify=y
    )
    
    # Step 2: Split Temp into Val (15% of total) and Test (15% of total) -> 50/50 of temp
    X_val, X_test, y_val, y_test = train_test_split(
        X_temp, y_temp,
        test_size=0.50,
        random_state=RANDOM_SEED,
        stratify=y_temp
    )

    # 8. Assemble DataFrames and Save
    train_df = pd.DataFrame({"text": X_train, "category": y_train})
    val_df = pd.DataFrame({"text": X_val, "category": y_val})
    test_df = pd.DataFrame({"text": X_test, "category": y_test})
    labeled_clean_df = pd.DataFrame({"text": X, "category": y})

    labeled_clean_df.to_csv(PROCESSED_DIR / "labeled_clean.csv", index=False)
    train_df.to_csv(PROCESSED_DIR / "train.csv", index=False)
    val_df.to_csv(PROCESSED_DIR / "val.csv", index=False)
    test_df.to_csv(PROCESSED_DIR / "test.csv", index=False)

    print("\n" + "=" * 70)
    print("  Stratified Partitions Generated (random_seed=42):")
    print("=" * 70)
    print(f"  Train Set      (70%): {len(train_df):>5} samples -> {PROCESSED_DIR / 'train.csv'}")
    print(f"  Validation Set (15%): {len(val_df):>5} samples -> {PROCESSED_DIR / 'val.csv'}")
    print(f"  Test Set       (15%): {len(test_df):>5} samples -> {PROCESSED_DIR / 'test.csv'}")
    print(f"  Total Cleaned       : {len(labeled_clean_df):>5} samples -> {PROCESSED_DIR / 'labeled_clean.csv'}")

    print("\nTrain class distribution:")
    print(train_df["category"].value_counts())
    print("\nPreprocessing complete. Ready for Task 3.2 (Feature Extraction).")


if __name__ == "__main__":
    run_preprocessing()
