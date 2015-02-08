//
//  CGSignInGateway.m
//  CityGo
//
//  Created by ruslan on 1/25/15.
//  Copyright (c) 2015 Ruslan Moskalenko. All rights reserved.
//

#import "CGSignInGateway.h"
#import "CGFacebookGateway.h"
#import "CGTwitterGateway.h"
#import "CGVkGateway.h"

static NSString *const kBasePassword = @"aFdeCbc550c9";

@implementation CGSignInGateway

- (void)signInUsingFBWithSuccess:(SignInSuccessBlock)successBlock failure:(SignInFailureBlock)failureBlock
{
    CGFacebookGateway *FBGateway = [CGFacebookGateway new];
    
    [FBGateway loginWithSuccess:^(NSDictionary *fbUser)
    {
        PFUser *user = [PFUser user];
        user.username = [NSString stringWithFormat:@"fb%@", fbUser[@"username"]];
        user.password = [NSString stringWithFormat:@"%@%@", fbUser[@"password"], kBasePassword];
        
        user[@"nickname"] = fbUser[@"nickname"];
        user[@"gender"] = fbUser[@"gender"];
        user[@"avatarURL"] = fbUser[@"avatarLink"];
        
        [user signUpInBackgroundWithBlock:^(BOOL succeeded, NSError *error)
        {
            if (!error)
            {
                // Hooray! Let them use the app now.
                successBlock(succeeded);
            }
            else
            {
                if (error.code == 202)
                {
                    [PFUser logInWithUsernameInBackground:user.username password:user.password block:^(PFUser *user, NSError *error)
                    {
                        if(!error)
                        {
                            successBlock(YES);
                        }
                        else
                        {
                            failureBlock(error);
                        }
                    }];
                }
            }
        }];
        
    }
    failure:^(NSError *error)
    {
        failureBlock(error);
    }];
}

- (void)signInUsingTwitter:(UIViewController *)controller WithSuccess:(SignInSuccessBlock)successBlock failure:(SignInFailureBlock)failureBlock
{
    CGTwitterGateway *twitterGateway = [CGTwitterGateway new];
    [twitterGateway login:controller WithSuccess:^(NSDictionary *twitterUser)
    {
        PFUser *user = [PFUser user];
        user.username = [NSString stringWithFormat:@"tw%@", twitterUser[@"id_str"]];
        user.password = [NSString stringWithFormat:@"%@%@", twitterUser[@"id_str"], kBasePassword];
        
        NSString *strAvatar = twitterUser[@"profile_image_url"];
        strAvatar = [strAvatar stringByReplacingOccurrencesOfString:@"_normal" withString:@""];
        
        user[@"avatarURL"] = strAvatar;
        user[@"nickname"] = twitterUser[@"screen_name"];
        
        [user signUpInBackgroundWithBlock:^(BOOL succeeded, NSError *error)
         {
             if (!error)
             {
                 // Hooray! Let them use the app now.
                 successBlock(succeeded);
             }
             else
             {
                 if (error.code == 202)
                 {
                     [PFUser logInWithUsernameInBackground:user.username password:user.password block:^(PFUser *user, NSError *error)
                      {
                          if(!error)
                          {
                              successBlock(YES);
                          }
                          else
                          {
                              failureBlock(error);
                          }
                      }];
                 }
             }
         }];
    }
    
    failure:^(NSError *error)
    {
        failureBlock(error);
    }];
}

- (void)signInUsingVkWithSuccess:(SignInSuccessBlock)successBlock failure:(SignInFailureBlock)failureBlock
{
    CGVkGateway *vkGateway = [CGVkGateway new];
    
    [vkGateway loginWithSuccess:^(NSDictionary *vkUser)
    {
        PFUser *user = [PFUser user];
        user.username = [NSString stringWithFormat:@"vk%@", [vkUser[@"id"] stringValue]];
        user.password = [NSString stringWithFormat:@"%@%@", [vkUser[@"id"] stringValue], kBasePassword];
        
        user[@"nickname"] = [NSString stringWithFormat:@"%@%@", vkUser[@"first_name"], vkUser[@"last_name"]];
        user[@"gender"] = [vkUser[@"sex"] integerValue] == 1 ? @"female" : @"male";
        user[@"avatarURL"] = vkUser[@"photo_400_orig"];
        user[@"birthday"] = vkUser[@"bdate"];
        
        [user signUpInBackgroundWithBlock:^(BOOL succeeded, NSError *error)
         {
             if (!error)
             {
                 // Hooray! Let them use the app now.
                 successBlock(succeeded);
             }
             else
             {
                 if (error.code == 202)
                 {
                     [PFUser logInWithUsernameInBackground:user.username password:user.password block:^(PFUser *user, NSError *error)
                      {
                          if(!error)
                          {
                              successBlock(YES);
                          }
                          else
                          {
                              failureBlock(error);
                          }
                      }];
                 }
             }
         }];
    }
    failure:^(NSError *error)
    {
        failureBlock(error);
    }];
}

- (void)signInUsingUserInfo:(NSDictionary *)userInfo WithSuccess:(SignInSuccessBlock)successBlock failure:(SignInFailureBlock)failureBlock
{
    [PFUser logInWithUsernameInBackground:userInfo[@"username"] password:userInfo[@"password"] block:^(PFUser *user, NSError *error)
     {
         if(!error)
         {
             successBlock(YES);
         }
         else
         {
             failureBlock(error);
         }
     }];
}

@end
