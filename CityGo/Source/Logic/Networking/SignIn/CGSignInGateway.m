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

@implementation CGSignInGateway

- (void)signInUsingFBWithSuccess:(SignInSuccessBlock)successBlock failure:(SignInFailureBlock)failureBlock
{
    CGFacebookGateway *FBGateway = [CGFacebookGateway new];
    
    [FBGateway loginWithSuccess:^(NSDictionary *fbUser)
    {
        PFUser *user = [PFUser user];
        user.username = fbUser[@"username"];
        user.password = fbUser[@"password"];
        user.email = fbUser[@"email"];
        
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
        user.username = twitterUser[@"screen_name"];
        user.password = twitterUser[@"id_str"];
        
        NSString *strAvatar = twitterUser[@"profile_image_url"];
        strAvatar = [strAvatar stringByReplacingOccurrencesOfString:@"_normal" withString:@""];
        
        user[@"avatarURL"] = strAvatar;
        
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
        user.username = [NSString stringWithFormat:@"%@%@", vkUser[@"last_name"], vkUser[@"first_name"]];
        user.password = [vkUser[@"id"] stringValue];
        
        user[@"gender"] = [vkUser[@"sex"] integerValue] == 1 ? @"female" : @"male";
        user[@"avatarURL"] = vkUser[@"photo_400_orig"];
        
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

@end
