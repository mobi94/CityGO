//
//  CGLoginViewController.m
//  CityGo
//
//  Created by ruslan on 1/22/15.
//  Copyright (c) 2015 Ruslan Moskalenko. All rights reserved.
//

#import "CGLoginViewController.h"
#import "CGViewControllerProtected.h"
#import "CGSignInProtocol.h"
#import "AppDelegate.h"
#import "FHSTwitterEngine.h"
#import <STAlertView/STAlertView.h>

static NSArray  *SCOPE = nil;

@interface CGLoginViewController () <FHSTwitterEngineAccessTokenDelegate, VKSdkDelegate, UIAlertViewDelegate>

@property(weak, nonatomic) IBOutlet id<CGSignInProtocol> loginner;

@property (weak, nonatomic) IBOutlet UIImageView *logoImageVIew;
@property (weak, nonatomic) IBOutlet UITextField *usernameTextField;
@property (weak, nonatomic) IBOutlet UITextField *passwordTextField;
@property (weak, nonatomic) IBOutlet UIButton *authButton;
@property (weak, nonatomic) IBOutlet UIButton *fogotPasswordButton;
@property (weak, nonatomic) IBOutlet UIButton *facebookButton;
@property (weak, nonatomic) IBOutlet UIButton *twitterButton;
@property (weak, nonatomic) IBOutlet UIButton *vkButton;
@property (weak, nonatomic) IBOutlet UILabel *privacyPolicyLabel;

@end

@implementation CGLoginViewController

- (void)viewDidAppear:(BOOL)animated
{
    
}

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    [[FHSTwitterEngine sharedEngine] setDelegate:self];
    
    [VKSdk initializeWithDelegate:self andAppId:@"4749201"];
    SCOPE = @[VK_PER_WALL, VK_PER_PHOTOS, VK_PER_NOHTTPS];
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (IBAction)authButtonClick:(id)sender
{
    [self performSegueWithIdentifier:@"signUpSegue" sender:sender];
}

#pragma mark -
#pragma mark Sign In

- (IBAction)signInViaFB:(id)sender
{
    [self showHUD];
    
    [self.loginner signInUsingFbWithBlock:^(NSError *error)
    {
        if (!error)
        {
            NSLog(@"SuccesLogin");
            
            AppDelegate *appDelegateTemp = [[UIApplication sharedApplication] delegate];
            appDelegateTemp.window.rootViewController = [self.storyboard instantiateInitialViewController];
        }
        else
        {
            [self handleError:error];
            
            NSLog(@"%@", [error description]);
        }
        [self hideHUD];
    }];
}

- (IBAction)signInViaTwitter:(id)sender
{
    [self showHUD];
    
    [self.loginner signInUsingTwitter:self WithBlock:^(NSError *error)
    {
        if (!error)
        {
            NSLog(@"SuccesLogin");
            
            AppDelegate *appDelegateTemp = [[UIApplication sharedApplication] delegate];
            appDelegateTemp.window.rootViewController = [self.storyboard instantiateInitialViewController];
        }
        else
        {
            [self handleError:error];
            
            NSLog(@"%@", [error description]);
        }
        [self hideHUD];
    }];
}

- (IBAction)signInViaVk:(id)sender
{
    [VKSdk authorize:SCOPE revokeAccess:YES forceOAuth:YES];
}

- (void)signInViaVk
{
    [self showHUD];
    
    [self.loginner signInUsingVkWithBlock:^(NSError *error)
    {
        if (!error)
        {
            NSLog(@"SuccesLogin");
            
            AppDelegate *appDelegateTemp = [[UIApplication sharedApplication] delegate];
            appDelegateTemp.window.rootViewController = [self.storyboard instantiateInitialViewController];
        }
        else
        {
            [self handleError:error];
            
            NSLog(@"%@", [error description]);
        }
        [self hideHUD];
    }];
}

- (IBAction)fogotPassword:(id)sender
{
    UIAlertView *recoveryPassword = [[UIAlertView alloc] initWithTitle:@"Restore Password" message:@"PLease, enter your account email" delegate:self cancelButtonTitle:@"Send" otherButtonTitles:@"Cancel", nil];
    [recoveryPassword setAlertViewStyle:UIAlertViewStylePlainTextInput];
    
    [recoveryPassword show];
}

#pragma mark -
#pragma mark - Alert View Delegate

- (void)alertView:(UIAlertView *)alertView clickedButtonAtIndex:(NSInteger)buttonIndex
{
    if (buttonIndex == 0)
    {
        UITextField *emailField = [alertView textFieldAtIndex:0];
        [PFUser requestPasswordResetForEmailInBackground:emailField.text];
    }
    else
    {
        NSLog(@"cancel");
    }
}

#pragma mark -
#pragma mark Twitter Engine Delegates

- (void)storeAccessToken:(NSString *)accessToken
{
    [[NSUserDefaults standardUserDefaults] setObject:accessToken forKey:@"SavedAccessHTTPBody"];
}

- (NSString *)loadAccessToken
{
    return [[NSUserDefaults standardUserDefaults] objectForKey:@"SavedAccessHTTPBody"];
}

#pragma mark -
#pragma mark VK Delegates

- (void)vkSdkNeedCaptchaEnter:(VKError *)captchaError
{
    VKCaptchaViewController *vc = [VKCaptchaViewController captchaControllerWithError:captchaError];
    [vc presentIn:self];
}

- (void)vkSdkTokenHasExpired:(VKAccessToken *)expiredToken
{
    [VKSdk authorize:SCOPE revokeAccess:NO];
}

- (void)vkSdkReceivedNewToken:(VKAccessToken *)newToken
{
    [self signInViaVk];
}

- (void)vkSdkShouldPresentViewController:(UIViewController *)controller
{
    [self presentViewController:controller animated:YES completion:nil];
}

- (void)vkSdkAcceptedUserToken:(VKAccessToken *)token
{
    [self signInViaVk];
}

- (void)vkSdkUserDeniedAccess:(VKError *)authorizationError
{
    NSLog(@"Access denied");
}

@end
